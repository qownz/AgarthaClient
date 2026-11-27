#version 330

uniform sampler2D InSampler;

layout(std140) uniform SamplerInfo {
	vec2 OutSize;
	vec2 InSize;
};

in vec2 texCoord;

out vec4 fragColor;

vec3 pickEntityRgb(vec4 center, vec4 left, vec4 right, vec4 up, vec4 down) {
	vec3 color = center.rgb;
	float best = center.a;
	if (left.a > best)  { best = left.a;  color = left.rgb;  }
	if (right.a > best) { best = right.a; color = right.rgb; }
	if (up.a > best)    { best = up.a;    color = up.rgb;    }
	if (down.a > best)  { best = down.a;  color = down.rgb;  }
	return color;
}

void main() {
	vec2 oneTexel = 1.0 / InSize;

	vec4 center = texture(InSampler, texCoord);
	vec4 left = texture(InSampler, texCoord - vec2(oneTexel.x, 0.0));
	vec4 right = texture(InSampler, texCoord + vec2(oneTexel.x, 0.0));
	vec4 up = texture(InSampler, texCoord - vec2(0.0, oneTexel.y));
	vec4 down = texture(InSampler, texCoord + vec2(0.0, oneTexel.y));

	float leftDiff = abs(center.a - left.a);
	float rightDiff = abs(center.a - right.a);
	float upDiff = abs(center.a - up.a);
	float downDiff = abs(center.a - down.a);
	float edge = clamp(leftDiff + rightDiff + upDiff + downDiff, 0.0, 1.0);

	if (edge <= 0.0) {
		fragColor = vec4(0.0);
		return;
	}

	vec3 entityRgb = pickEntityRgb(center, left, right, up, down);
	float peak = max(max(entityRgb.r, entityRgb.g), entityRgb.b);
	if (peak > 0.001) {
		entityRgb /= peak;
	}

	fragColor = vec4(entityRgb, min(edge * 2.5, 1.0));
}
