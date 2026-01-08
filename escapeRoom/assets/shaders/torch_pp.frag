#ifdef GL_ES
precision mediump float;
#endif

// The next line is a special string that indicates to the AbstractShader to include the util.glsl file here.
// *****IMPORT: util.glsl*****

// ----- From vertex shader -----
varying vec2 uv;
varying vec2 worldPos;

// ----- From LibGDX -----
uniform sampler2D u_texture;

// ----- Common uniforms set by DrawSystem -----
uniform vec2 u_resolution;
uniform float u_time;
uniform vec2 u_mouse;
uniform vec2 u_texelSize;
uniform vec2 u_aspect;

// ----- Custom uniforms -----
uniform float u_viewDistance; // 0 to 1, where 1 means full screen is visible
const float u_viewFalloff = 0.25; // portion of viewDistance used for falloff
uniform int u_lightCount; // number of active light sources
const int MAX_LIGHTS = 128; // maximum number of light sources supported
uniform vec3 u_lights[MAX_LIGHTS]; // light positions (x, y) and radius (z)
uniform float u_vignetteRadius; // radius of the vignette (configurable)
uniform float u_baseDimness; // base dimness of the screen (0.0 to 1.0)
uniform int u_areaCount; // number of illuminated areas
const int MAX_AREAS = 32; // maximum number of illuminated areas
uniform vec4 u_areas[MAX_AREAS]; // illuminated areas (x, y, width, height)

// ----- Custom functions -----
// Hash function for pseudo-random values based on position
float hash(vec2 p) {
    return fract(sin(dot(p, vec2(12.9898, 78.233))) * 43758.5453);
}

// Perlin-like noise using smoothstep (Smoothstep noise, works well in GLSL)
float smoothNoise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);

    // Smooth interpolation curve (Hermite curve)
    vec2 u = f * f * (3.0 - 2.0 * f);

    // Hash values at corners
    float n00 = hash(i + vec2(0.0, 0.0));
    float n10 = hash(i + vec2(1.0, 0.0));
    float n01 = hash(i + vec2(0.0, 1.0));
    float n11 = hash(i + vec2(1.0, 1.0));

    // Interpolate
    float nx0 = mix(n00, n10, u.x);
    float nx1 = mix(n01, n11, u.x);
    return mix(nx0, nx1, u.y);
}

// Multi-octave Perlin-like noise for more natural flickering
float perlinNoise(vec2 p) {
    float value = 0.0;
    float amplitude = 1.0;
    float frequency = 1.0;
    float maxValue = 0.0;

    // Use 3 octaves for natural variation
    for (int i = 0; i < 3; i++) {
        value += amplitude * smoothNoise(p * frequency);
        maxValue += amplitude;
        amplitude *= 0.5;
        frequency *= 2.0;
    }

    return value / maxValue;
}


// Calculate illumination with natural Perlin-based flickering
float calculateLightIllumination(vec2 pos, vec3 light) {
    float dist = distance(pos, light.xy);
    float radius = light.z;

    // Create a unique seed for this light
    float lightSeed = hash(light.xy);

    // Use slower time scale for natural flicker (slower = more realistic)
    float slowTime = u_time * 1.0; // Slower flickering

    // Combine multiple noise layers for natural variation
    float noiseValue = perlinNoise(vec2(lightSeed * 10.0, slowTime * 0.3));
    float noiseValue2 = perlinNoise(vec2(lightSeed * 20.0 + 5.0, slowTime * 0.7));

    // Blend the noise values with different weights
    float combinedNoise = mix(noiseValue, noiseValue2, 0.5);

    // Create natural flicker: occasional dimming with smooth transitions
    // Range: 0.6 to 1.0 (60% to 100% brightness)
    float flicker = mix(0.6, 1.0, combinedNoise);

    // Add occasional stronger flickers for realism
    float strongFlicker = perlinNoise(vec2(lightSeed * 5.0, slowTime * 0.15));
    flicker = mix(flicker, strongFlicker * 0.8 + 0.5, 0.2);

    float flickerRadius = radius * flicker;

    // Smooth falloff from center to radius
    float t = smoothstep(0.0, flickerRadius, dist);
    t = 1.0 - t;
    t = pow(t, 2.0);

    return t;
}

// Check if a position is within any illuminated area with smooth falloff at borders
float checkIlluminatedAreas(vec2 pos) {
    float maxArea = 0.0;
    for (int i = 0; i < MAX_AREAS; i++) {
        if (i >= u_areaCount) break;

        vec4 area = u_areas[i];
        float areaX = area.x;
        float areaY = area.y;
        float areaWidth = area.z;
        float areaHeight = area.w;

        // Calculate distance from rectangle center
        float centerX = areaX + areaWidth * 0.5;
        float centerY = areaY + areaHeight * 0.5;
        float halfWidth = areaWidth * 0.5;
        float halfHeight = areaHeight * 0.5;
        float cornerRadius = min(halfWidth, halfHeight) * 0.15; // 15% of smallest dimension

        // Distance to nearest edge of rectangle (from inside)
        float dx = abs(pos.x - centerX) - halfWidth + cornerRadius;
        float dy = abs(pos.y - centerY) - halfHeight + cornerRadius;
        float distToEdge = length(max(vec2(dx, dy), vec2(0.0)));

        // Smooth falloff starting at the border, fading over 5 units inward
        float falloff = smoothstep(5.0, 0.0, distToEdge);

        maxArea = max(maxArea, falloff);
    }
    return maxArea;
}

// ----- Main -----
void main() {
    vec4 color = unPma(texture2D(u_texture, uv));

    // Calculate illumination from all light sources
    float totalIllumination = 0.0;
    for (int i = 0; i < MAX_LIGHTS; i++) {
        if (i >= u_lightCount) break; // Only process active lights
        totalIllumination = max(totalIllumination, calculateLightIllumination(worldPos, u_lights[i]));
    }

    // Check if we're in an illuminated area
    float areaIllumination = checkIlluminatedAreas(worldPos);
    totalIllumination = max(totalIllumination, areaIllumination);

    // Circular vignette for dark corners
    // We want: 1.0 at center (visible), 0.0 at edges (black)
    vec2 aspectUv = uv / u_aspect + vec2(0.5) * (1.0 - 1.0 / u_aspect);
    vec2 centerDist = aspectUv - vec2(0.5);
    float dist = length(centerDist);

    // Create vignette that fades from 1.0 at center to 0.0 at edges
    // Using u_vignetteRadius for configurable vignette size
    float vignetteAlpha = smoothstep(u_vignetteRadius + u_viewFalloff, u_vignetteRadius, dist);
    vignetteAlpha = pow(vignetteAlpha, 2.5);

    // Define brightness zones:
    // - Torch areas: fully bright (1.0)
    // - Rest of screen: dimmed (u_baseDimness)
    // - Edges (vignette): black (0.0)

    // Determine brightness in the screen (before vignette)
    float screenBrightness = mix(u_baseDimness, 1.0, totalIllumination);

    // Apply vignette: fade to black at edges
    // BUT: Allow torch light to break through the vignette
    // vignetteAlpha: 1.0 at center, 0.0 at edges
    // totalIllumination: 0.0 = no light, 1.0 = full light
    // If we have torch illumination, it pierces through the vignette
    float vignetteBreakthrough = smoothstep(0.0, 0.1, totalIllumination);
    float finalBrightness = mix(
        u_baseDimness * vignetteAlpha,  // No torch light: vignette dims the base
        screenBrightness,                // Torch light: full brightness, pierces vignette
        vignetteBreakthrough
    );

    color.rgb *= finalBrightness;

    gl_FragColor = pma(color);
}
