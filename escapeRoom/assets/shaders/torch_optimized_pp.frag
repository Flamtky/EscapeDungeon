#ifdef GL_ES
precision mediump float;
#endif

// *****IMPORT: util.glsl*****

varying vec2 uv;
varying vec2 worldPos;

uniform sampler2D u_texture;
uniform sampler2D u_noiseTexture; // New: A small tiling noise texture

uniform vec2 u_resolution;
uniform float u_time;
uniform vec2 u_mouse;
uniform vec2 u_texelSize;
uniform vec2 u_aspect;

// Light Uniforms
uniform float u_viewDistance;
const float u_viewFalloff = 0.25;
uniform int u_lightCount;
const int MAX_LIGHTS = 128;
uniform vec3 u_lights[MAX_LIGHTS];

uniform float u_vignetteRadius;
uniform float u_baseDimness;

// Area Uniforms
uniform int u_areaCount;
const int MAX_AREAS = 32;
uniform vec4 u_areas[MAX_AREAS];

// --- OPTIMIZED LIGHTING ---
float calculateLightIllumination(vec2 pos, vec3 light) {
    vec2 diff = pos - light.xy;
    float distSq = dot(diff, diff); // Use dot product for squared distance (no sqrt)

    float radius = light.z;
    float radiusSq = radius * radius;

    // Early exit if pixel is way outside the light radius
    if (distSq > radiusSq * 1.5) return 0.0;

    // OPTIMIZED FLICKER: Use a noise texture instead of procedural Perlin
    // This replaces ~50+ math operations with 1 texture fetch
    vec2 noiseCoord = (light.xy * 0.1) + vec2(u_time * 0.05);
    float flicker = texture2D(u_noiseTexture, noiseCoord).r;

    // Remap flicker to 0.7 - 1.0 range
    flicker = 0.7 + (flicker * 0.3);
    float flickerRadiusSq = radiusSq * (flicker * flicker);

    // Smooth falloff using squared distance
    float t = clamp(1.0 - (distSq / flickerRadiusSq), 0.0, 1.0);
    return t * t; // Quadratic falloff
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

// --- REMAINDER OF SHADER (checkIlluminatedAreas, etc) ---
// ... keep your checkIlluminatedAreas as is ...

void main() {
    vec4 color = unPma(texture2D(u_texture, uv));

    float totalIllumination = 0.0;

    // Optimization: Step through lights
    for (int i = 0; i < MAX_LIGHTS; i++) {
        if (i >= u_lightCount) break;
        totalIllumination = max(totalIllumination, calculateLightIllumination(worldPos, u_lights[i]));
    }

    float areaIllumination = checkIlluminatedAreas(worldPos);
    totalIllumination = max(totalIllumination, areaIllumination);

    // Vignette math stays the same as it's based on screen UVs
    vec2 aspectUv = uv / u_aspect + vec2(0.5) * (1.0 - 1.0 / u_aspect);
    float dist = length(aspectUv - vec2(0.5));
    float vignetteAlpha = smoothstep(u_vignetteRadius + u_viewFalloff, u_vignetteRadius, dist);

    float screenBrightness = mix(u_baseDimness, 1.0, totalIllumination);
    float vignetteBreakthrough = smoothstep(0.0, 0.1, totalIllumination);
    float finalBrightness = mix(u_baseDimness * vignetteAlpha, screenBrightness, vignetteBreakthrough);

    color.rgb *= finalBrightness;
    gl_FragColor = pma(color);
}
