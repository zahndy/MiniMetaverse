/**
 * Copyright (c) 2008, openmetaverse.org
 * Copyright (c) 2009-2011, Frederick Martian
 * All rights reserved.
 *
 * - Redistribution and use in source and binary forms, with or without
 *   modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 * - Neither the name of the openmetaverse.org nor the names
 *   of its contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package libomv.types;

import libomv.utils.Helpers;
import libomv.utils.RefObject;

public final class Matrix4
{
	public float M11, M12, M13, M14;
	public float M21, M22, M23, M24;
	public float M31, M32, M33, M34;
	public float M41, M42, M43, M44;

	public Vector3 getAtAxis()
	{
		return new Vector3(M11, M21, M31);
	}

	public void setAtAxis(Vector3 value)
	{
		M11 = value.X;
		M21 = value.Y;
		M31 = value.Z;
	}

	public Vector3 getLeftAxis()
	{
		return new Vector3(M12, M22, M32);
	}

	public void setLeftAxis(Vector3 value)
	{
		M12 = value.X;
		M22 = value.Y;
		M32 = value.Z;
	}

	public Vector3 getUpAxis()
	{
		return new Vector3(M13, M23, M33);
	}

	public void setUpAxis(Vector3 value)
	{
		M13 = value.X;
		M23 = value.Y;
		M33 = value.Z;
	}

	public Matrix4()
	{
		M11 = M12 = M13 = M14 = 0.0f;
		M21 = M22 = M23 = M24 = 0.0f;
		M31 = M32 = M33 = M34 = 0.0f;
		M41 = M42 = M43 = M44 = 0.0f;
	}

	public Matrix4(float m11, float m12, float m13, float m14, float m21, float m22, float m23, float m24, float m31,
			float m32, float m33, float m34, float m41, float m42, float m43, float m44)
	{
		M11 = m11;
		M12 = m12;
		M13 = m13;
		M14 = m14;

		M21 = m21;
		M22 = m22;
		M23 = m23;
		M24 = m24;

		M31 = m31;
		M32 = m32;
		M33 = m33;
		M34 = m34;

		M41 = m41;
		M42 = m42;
		M43 = m43;
		M44 = m44;
	}

	public Matrix4(float roll, float pitch, float yaw)
	{
		Matrix4 m = CreateFromEulers(roll, pitch, yaw);
		M11 = m.M11;
		M12 = m.M12;
		M13 = m.M13;
		M14 = m.M14;

		M21 = m.M21;
		M22 = m.M22;
		M23 = m.M23;
		M24 = m.M24;

		M31 = m.M31;
		M32 = m.M32;
		M33 = m.M33;
		M34 = m.M34;

		M41 = m.M41;
		M42 = m.M42;
		M43 = m.M43;
		M44 = m.M44;
	}

	public Matrix4(Matrix4 m)
	{
		M11 = m.M11;
		M12 = m.M12;
		M13 = m.M13;
		M14 = m.M14;

		M21 = m.M21;
		M22 = m.M22;
		M23 = m.M23;
		M24 = m.M24;

		M31 = m.M31;
		M32 = m.M32;
		M33 = m.M33;
		M34 = m.M34;

		M41 = m.M41;
		M42 = m.M42;
		M43 = m.M43;
		M44 = m.M44;
	}

	public float Determinant()
	{
		return M14 * M23 * M32 * M41 - M13 * M24 * M32 * M41 - M14 * M22 * M33 * M41 + M12 * M24 * M33 * M41 + M13
				* M22 * M34 * M41 - M12 * M23 * M34 * M41 - M14 * M23 * M31 * M42 + M13 * M24 * M31 * M42 + M14 * M21
				* M33 * M42 - M11 * M24 * M33 * M42 - M13 * M21 * M34 * M42 + M11 * M23 * M34 * M42 + M14 * M22 * M31
				* M43 - M12 * M24 * M31 * M43 - M14 * M21 * M32 * M43 + M11 * M24 * M32 * M43 + M12 * M21 * M34 * M43
				- M11 * M22 * M34 * M43 - M13 * M22 * M31 * M44 + M12 * M23 * M31 * M44 + M13 * M21 * M32 * M44 - M11
				* M23 * M32 * M44 - M12 * M21 * M33 * M44 + M11 * M22 * M33 * M44;
	}

	public float Determinant3x3()
	{
		float det = 0f;

		float diag1 = M11 * M22 * M33;
		float diag2 = M12 * M32 * M31;
		float diag3 = M13 * M21 * M32;
		float diag4 = M31 * M22 * M13;
		float diag5 = M32 * M23 * M11;
		float diag6 = M33 * M21 * M12;

		det = diag1 + diag2 + diag3 - (diag4 + diag5 + diag6);

		return det;
	}

	public float Trace()
	{
		return M11 + M22 + M33 + M44;
	}

	/**
	 * Convert this matrix to euler rotations
	 * 
	 * @param roll
	 *            X euler angle
	 * @param pitch
	 *            Y euler angle
	 * @param yaw
	 *            Z euler angle
	 */
	public void GetEulerAngles(RefObject<Float> roll, RefObject<Float> pitch, RefObject<Float> yaw)
	{
		double angleX, angleY, angleZ;
		double cx, cy, cz; // cosines
		double sx, sz; // sines

		angleY = Math.asin(Helpers.Clamp(M13, -1f, 1f));
		cy = Math.cos(angleY);

		if (Math.abs(cy) > 0.005f)
		{
			// No gimbal lock
			cx = M33 / cy;
			sx = (-M23) / cy;

			angleX = (float) Math.atan2(sx, cx);

			cz = M11 / cy;
			sz = (-M12) / cy;

			angleZ = (float) Math.atan2(sz, cz);
		}
		else
		{
			// Gimbal lock
			angleX = 0;

			cz = M22;
			sz = M21;

			angleZ = Math.atan2(sz, cz);
		}

		// Return only positive angles in [0,360]
		if (angleX < 0)
		{
			angleX += 360d;
		}
		if (angleY < 0)
		{
			angleY += 360d;
		}
		if (angleZ < 0)
		{
			angleZ += 360d;
		}

		roll.argvalue = (float) angleX;
		pitch.argvalue = (float) angleY;
		yaw.argvalue = (float) angleZ;
	}

	/**
	 * Convert this matrix to a quaternion rotation
	 * 
	 * @return A quaternion representation of this rotation matrix
	 */
	public Quaternion GetQuaternion()
	{
		Quaternion quat = new Quaternion();
		float trace = Trace() + 1f;

		if (trace > Helpers.FLOAT_MAG_THRESHOLD)
		{
			float s = 0.5f / (float) Math.sqrt(trace);

			quat.X = (M32 - M23) * s;
			quat.Y = (M13 - M31) * s;
			quat.Z = (M21 - M12) * s;
			quat.W = 0.25f / s;
		}
		else
		{
			if (M11 > M22 && M11 > M33)
			{
				float s = 2.0f * (float) Math.sqrt(1.0f + M11 - M22 - M33);

				quat.X = 0.25f * s;
				quat.Y = (M12 + M21) / s;
				quat.Z = (M13 + M31) / s;
				quat.W = (M23 - M32) / s;
			}
			else if (M22 > M33)
			{
				float s = 2.0f * (float) Math.sqrt(1.0f + M22 - M11 - M33);

				quat.X = (M12 + M21) / s;
				quat.Y = 0.25f * s;
				quat.Z = (M23 + M32) / s;
				quat.W = (M13 - M31) / s;
			}
			else
			{
				float s = 2.0f * (float) Math.sqrt(1.0f + M33 - M11 - M22);

				quat.X = (M13 + M31) / s;
				quat.Y = (M23 + M32) / s;
				quat.Z = 0.25f * s;
				quat.W = (M12 - M21) / s;
			}
		}

		return quat;
	}

	public static Matrix4 CreateFromAxisAngle(Vector3 axis, float angle)
	{
		Matrix4 matrix = new Matrix4();

		float x = axis.X;
		float y = axis.Y;
		float z = axis.Z;
		float sin = (float) Math.sin(angle);
		float cos = (float) Math.cos(angle);
		float xx = x * x;
		float yy = y * y;
		float zz = z * z;
		float xy = x * y;
		float xz = x * z;
		float yz = y * z;

		matrix.M11 = xx + (cos * (1f - xx));
		matrix.M12 = (xy - (cos * xy)) + (sin * z);
		matrix.M13 = (xz - (cos * xz)) - (sin * y);
		// matrix.M14 = 0f;

		matrix.M21 = (xy - (cos * xy)) - (sin * z);
		matrix.M22 = yy + (cos * (1f - yy));
		matrix.M23 = (yz - (cos * yz)) + (sin * x);
		// matrix.M24 = 0f;

		matrix.M31 = (xz - (cos * xz)) + (sin * y);
		matrix.M32 = (yz - (cos * yz)) - (sin * x);
		matrix.M33 = zz + (cos * (1f - zz));
		// matrix.M34 = 0f;

		// matrix.M41 = matrix.M42 = matrix.M43 = 0f;
		matrix.M44 = 1f;

		return matrix;
	}

	/**
	 * Construct a matrix from euler rotation values in radians
	 * 
	 * @param roll
	 *            X euler angle in radians
	 * @param pitch
	 *            Y euler angle in radians
	 * @param yaw
	 *            Z euler angle in radians
	 */
	public static Matrix4 CreateFromEulers(float roll, float pitch, float yaw)
	{
		Matrix4 m = new Matrix4();

		float a, b, c, d, e, f;
		float ad, bd;

		a = (float) Math.cos(roll);
		b = (float) Math.sin(roll);
		c = (float) Math.cos(pitch);
		d = (float) Math.sin(pitch);
		e = (float) Math.cos(yaw);
		f = (float) Math.sin(yaw);

		ad = a * d;
		bd = b * d;

		m.M11 = c * e;
		m.M12 = -c * f;
		m.M13 = d;
		m.M14 = 0f;

		m.M21 = bd * e + a * f;
		m.M22 = -bd * f + a * e;
		m.M23 = -b * c;
		m.M24 = 0f;

		m.M31 = -ad * e + b * f;
		m.M32 = ad * f + b * e;
		m.M33 = a * c;
		m.M34 = 0f;

		m.M41 = m.M42 = m.M43 = 0f;
		m.M44 = 1f;

		return m;
	}

	public static Matrix4 CreateFromQuaternion(Quaternion quaternion)
	{
		float xx = quaternion.X * quaternion.X;
		float yy = quaternion.Y * quaternion.Y;
		float zz = quaternion.Z * quaternion.Z;
		float xy = quaternion.X * quaternion.Y;
		float zw = quaternion.Z * quaternion.W;
		float zx = quaternion.Z * quaternion.X;
		float yw = quaternion.Y * quaternion.W;
		float yz = quaternion.Y * quaternion.Z;
		float xw = quaternion.X * quaternion.W;

		return new Matrix4(1f - (2f * (yy + zz)), 2f * (xy + zw), 2f * (zx - yw), 0f, 2f * (xy - zw),
				1f - (2f * (zz + xx)), 2f * (yz + xw), 0f, 2f * (zx + yw), 2f * (yz - xw), 1f - (2f * (yy + xx)), 0f,
				0f, 0f, 0f, 1f);
	}

	public static Matrix4 CreateLookAt(Vector3 cameraPosition, Vector3 cameraTarget, Vector3 cameraUpVector)
	{

		Vector3 z = Vector3.Normalize(Vector3.subtract(cameraPosition, cameraTarget));
		Vector3 x = Vector3.Normalize(Vector3.Cross(cameraUpVector, z));
		Vector3 y = Vector3.Cross(z, x);

		return new Matrix4(x.X, y.X, z.X, 0f, x.Y, y.Y, z.Y, 0f, x.Z, y.Z, z.Z, 0f, -Vector3.Dot(x, cameraPosition),
				-Vector3.Dot(y, cameraPosition), -Vector3.Dot(z, cameraPosition), 1f);
	}

	public static Matrix4 CreateRotationX(float radians)
	{
		float cos = (float) Math.cos(radians);
		float sin = (float) Math.sin(radians);

		return new Matrix4(1f, 0f, 0f, 0f, 0f, cos, sin, 0f, 0f, -sin, cos, 0f, 0f, 0f, 0f, 1f);
	}

	public static Matrix4 CreateRotationY(float radians)
	{
		Matrix4 matrix = new Matrix4();

		float cos = (float) Math.cos(radians);
		float sin = (float) Math.sin(radians);

		matrix.M11 = cos;
		matrix.M12 = 0f;
		matrix.M13 = -sin;
		matrix.M14 = 0f;

		matrix.M21 = 0f;
		matrix.M22 = 1f;
		matrix.M23 = 0f;
		matrix.M24 = 0f;

		matrix.M31 = sin;
		matrix.M32 = 0f;
		matrix.M33 = cos;
		matrix.M34 = 0f;

		matrix.M41 = 0f;
		matrix.M42 = 0f;
		matrix.M43 = 0f;
		matrix.M44 = 1f;

		return matrix;
	}

	public static Matrix4 CreateRotationZ(float radians)
	{
		Matrix4 matrix = new Matrix4();

		float cos = (float) Math.cos(radians);
		float sin = (float) Math.sin(radians);

		matrix.M11 = cos;
		matrix.M12 = sin;
		matrix.M13 = 0f;
		matrix.M14 = 0f;

		matrix.M21 = -sin;
		matrix.M22 = cos;
		matrix.M23 = 0f;
		matrix.M24 = 0f;

		matrix.M31 = 0f;
		matrix.M32 = 0f;
		matrix.M33 = 1f;
		matrix.M34 = 0f;

		matrix.M41 = 0f;
		matrix.M42 = 0f;
		matrix.M43 = 0f;
		matrix.M44 = 1f;

		return matrix;
	}

	public static Matrix4 CreateScale(Vector3 scale)
	{
		Matrix4 matrix = new Matrix4();

		matrix.M11 = scale.X;
		matrix.M12 = 0f;
		matrix.M13 = 0f;
		matrix.M14 = 0f;

		matrix.M21 = 0f;
		matrix.M22 = scale.Y;
		matrix.M23 = 0f;
		matrix.M24 = 0f;

		matrix.M31 = 0f;
		matrix.M32 = 0f;
		matrix.M33 = scale.Z;
		matrix.M34 = 0f;

		matrix.M41 = 0f;
		matrix.M42 = 0f;
		matrix.M43 = 0f;
		matrix.M44 = 1f;

		return matrix;
	}

	public static Matrix4 CreateTranslation(Vector3 position)
	{
		Matrix4 matrix = new Matrix4();

		matrix.M11 = 1f;
		matrix.M12 = 0f;
		matrix.M13 = 0f;
		matrix.M14 = 0f;

		matrix.M21 = 0f;
		matrix.M22 = 1f;
		matrix.M23 = 0f;
		matrix.M24 = 0f;

		matrix.M31 = 0f;
		matrix.M32 = 0f;
		matrix.M33 = 1f;
		matrix.M34 = 0f;

		matrix.M41 = position.X;
		matrix.M42 = position.Y;
		matrix.M43 = position.Z;
		matrix.M44 = 1f;

		return matrix;
	}

	public static Matrix4 CreateWorld(Vector3 position, Vector3 forward, Vector3 up)
	{
		Matrix4 result = new Matrix4();

		// Normalize forward vector
		forward.Normalize();

		// Calculate right vector
		Vector3 right = Vector3.Cross(forward, up);
		right.Normalize();

		// Recalculate up vector
		up = Vector3.Cross(right, forward);
		up.Normalize();

		result.M11 = right.X;
		result.M12 = right.Y;
		result.M13 = right.Z;
		result.M14 = 0.0f;

		result.M21 = up.X;
		result.M22 = up.Y;
		result.M23 = up.Z;
		result.M24 = 0.0f;

		result.M31 = -forward.X;
		result.M32 = -forward.Y;
		result.M33 = -forward.Z;
		result.M34 = 0.0f;

		result.M41 = position.X;
		result.M42 = position.Y;
		result.M43 = position.Z;
		result.M44 = 1.0f;

		return result;
	}

	public static Matrix4 Lerp(Matrix4 matrix1, Matrix4 matrix2, float amount)
	{
		return new Matrix4(matrix1.M11 + ((matrix2.M11 - matrix1.M11) * amount), matrix1.M12
				+ ((matrix2.M12 - matrix1.M12) * amount), matrix1.M13 + ((matrix2.M13 - matrix1.M13) * amount),
				matrix1.M14 + ((matrix2.M14 - matrix1.M14) * amount),

				matrix1.M21 + ((matrix2.M21 - matrix1.M21) * amount), matrix1.M22
						+ ((matrix2.M22 - matrix1.M22) * amount), matrix1.M23 + ((matrix2.M23 - matrix1.M23) * amount),
				matrix1.M24 + ((matrix2.M24 - matrix1.M24) * amount),

				matrix1.M31 + ((matrix2.M31 - matrix1.M31) * amount), matrix1.M32
						+ ((matrix2.M32 - matrix1.M32) * amount), matrix1.M33 + ((matrix2.M33 - matrix1.M33) * amount),
				matrix1.M34 + ((matrix2.M34 - matrix1.M34) * amount),

				matrix1.M41 + ((matrix2.M41 - matrix1.M41) * amount), matrix1.M42
						+ ((matrix2.M42 - matrix1.M42) * amount), matrix1.M43 + ((matrix2.M43 - matrix1.M43) * amount),
				matrix1.M44 + ((matrix2.M44 - matrix1.M44) * amount));
	}

	public static Matrix4 negate(Matrix4 matrix)
	{
		return new Matrix4(-matrix.M11, -matrix.M12, -matrix.M13, -matrix.M14, -matrix.M21, -matrix.M22, -matrix.M23,
				-matrix.M24, -matrix.M31, -matrix.M32, -matrix.M33, -matrix.M34, -matrix.M41, -matrix.M42, -matrix.M43,
				-matrix.M44);
	}

	public static Matrix4 add(Matrix4 matrix1, Matrix4 matrix2)
	{
		return new Matrix4(matrix1.M11 + matrix2.M11, matrix1.M12 + matrix2.M12, matrix1.M13 + matrix2.M13, matrix1.M14
				+ matrix2.M14, matrix1.M21 + matrix2.M21, matrix1.M22 + matrix2.M22, matrix1.M23 + matrix2.M23,
				matrix1.M24 + matrix2.M24, matrix1.M31 + matrix2.M31, matrix1.M32 + matrix2.M32, matrix1.M33
						+ matrix2.M33, matrix1.M34 + matrix2.M34, matrix1.M41 + matrix2.M41, matrix1.M42 + matrix2.M42,
				matrix1.M43 + matrix2.M43, matrix1.M44 + matrix2.M44);
	}

	public static Matrix4 subtract(Matrix4 matrix1, Matrix4 matrix2)
	{
		return new Matrix4(matrix1.M11 - matrix2.M11, matrix1.M12 - matrix2.M12, matrix1.M13 - matrix2.M13, matrix1.M14
				- matrix2.M14, matrix1.M21 - matrix2.M21, matrix1.M22 - matrix2.M22, matrix1.M23 - matrix2.M23,
				matrix1.M24 - matrix2.M24, matrix1.M31 - matrix2.M31, matrix1.M32 - matrix2.M32, matrix1.M33
						- matrix2.M33, matrix1.M34 - matrix2.M34, matrix1.M41 - matrix2.M41, matrix1.M42 - matrix2.M42,
				matrix1.M43 - matrix2.M43, matrix1.M44 - matrix2.M44);
	}

	public static Matrix4 multiply(Matrix4 matrix1, Matrix4 matrix2)
	{
		return new Matrix4(matrix1.M11 * matrix2.M11 + matrix1.M12 * matrix2.M21 + matrix1.M13 * matrix2.M31
				+ matrix1.M14 * matrix2.M41, matrix1.M11 * matrix2.M12 + matrix1.M12 * matrix2.M22 + matrix1.M13
				* matrix2.M32 + matrix1.M14 * matrix2.M42, matrix1.M11 * matrix2.M13 + matrix1.M12 * matrix2.M23
				+ matrix1.M13 * matrix2.M33 + matrix1.M14 * matrix2.M43, matrix1.M11 * matrix2.M14 + matrix1.M12
				* matrix2.M24 + matrix1.M13 * matrix2.M34 + matrix1.M14 * matrix2.M44, matrix1.M21 * matrix2.M11
				+ matrix1.M22 * matrix2.M21 + matrix1.M23 * matrix2.M31 + matrix1.M24 * matrix2.M41, matrix1.M21
				* matrix2.M12 + matrix1.M22 * matrix2.M22 + matrix1.M23 * matrix2.M32 + matrix1.M24 * matrix2.M42,
				matrix1.M21 * matrix2.M13 + matrix1.M22 * matrix2.M23 + matrix1.M23 * matrix2.M33 + matrix1.M24
						* matrix2.M43, matrix1.M21 * matrix2.M14 + matrix1.M22 * matrix2.M24 + matrix1.M23
						* matrix2.M34 + matrix1.M24 * matrix2.M44, matrix1.M31 * matrix2.M11 + matrix1.M32
						* matrix2.M21 + matrix1.M33 * matrix2.M31 + matrix1.M34 * matrix2.M41, matrix1.M31
						* matrix2.M12 + matrix1.M32 * matrix2.M22 + matrix1.M33 * matrix2.M32 + matrix1.M34
						* matrix2.M42, matrix1.M31 * matrix2.M13 + matrix1.M32 * matrix2.M23 + matrix1.M33
						* matrix2.M33 + matrix1.M34 * matrix2.M43, matrix1.M31 * matrix2.M14 + matrix1.M32
						* matrix2.M24 + matrix1.M33 * matrix2.M34 + matrix1.M34 * matrix2.M44, matrix1.M41
						* matrix2.M11 + matrix1.M42 * matrix2.M21 + matrix1.M43 * matrix2.M31 + matrix1.M44
						* matrix2.M41, matrix1.M41 * matrix2.M12 + matrix1.M42 * matrix2.M22 + matrix1.M43
						* matrix2.M32 + matrix1.M44 * matrix2.M42, matrix1.M41 * matrix2.M13 + matrix1.M42
						* matrix2.M23 + matrix1.M43 * matrix2.M33 + matrix1.M44 * matrix2.M43, matrix1.M41
						* matrix2.M14 + matrix1.M42 * matrix2.M24 + matrix1.M43 * matrix2.M34 + matrix1.M44
						* matrix2.M44);
	}

	public static Matrix4 multiply(Matrix4 matrix1, float scaleFactor)
	{
		return new Matrix4(matrix1.M11 * scaleFactor, matrix1.M12 * scaleFactor, matrix1.M13 * scaleFactor, matrix1.M14
				* scaleFactor, matrix1.M21 * scaleFactor, matrix1.M22 * scaleFactor, matrix1.M23 * scaleFactor,
				matrix1.M24 * scaleFactor, matrix1.M31 * scaleFactor, matrix1.M32 * scaleFactor, matrix1.M33
						* scaleFactor, matrix1.M34 * scaleFactor, matrix1.M41 * scaleFactor, matrix1.M42 * scaleFactor,
				matrix1.M43 * scaleFactor, matrix1.M44 * scaleFactor);
	}

	public static Matrix4 divide(Matrix4 matrix1, Matrix4 matrix2)
	{
		return new Matrix4(matrix1.M11 / matrix2.M11, matrix1.M12 / matrix2.M12, matrix1.M13 / matrix2.M13, matrix1.M14
				/ matrix2.M14, matrix1.M21 / matrix2.M21, matrix1.M22 / matrix2.M22, matrix1.M23 / matrix2.M23,
				matrix1.M24 / matrix2.M24, matrix1.M31 / matrix2.M31, matrix1.M32 / matrix2.M32, matrix1.M33
						/ matrix2.M33, matrix1.M34 / matrix2.M34, matrix1.M41 / matrix2.M41, matrix1.M42 / matrix2.M42,
				matrix1.M43 / matrix2.M43, matrix1.M44 / matrix2.M44);
	}

	public static Matrix4 divide(Matrix4 matrix1, float divider)
	{
		float oodivider = 1f / divider;
		return new Matrix4(matrix1.M11 * oodivider, matrix1.M12 * oodivider, matrix1.M13 * oodivider, matrix1.M14
				* oodivider, matrix1.M21 * oodivider, matrix1.M22 * oodivider, matrix1.M23 * oodivider, matrix1.M24
				* oodivider, matrix1.M31 * oodivider, matrix1.M32 * oodivider, matrix1.M33 * oodivider, matrix1.M34
				* oodivider, matrix1.M41 * oodivider, matrix1.M42 * oodivider, matrix1.M43 * oodivider, matrix1.M44
				* oodivider);
	}

	public static Matrix4 Transform(Matrix4 value, Quaternion rotation)
	{
		float x2 = rotation.X + rotation.X;
		float y2 = rotation.Y + rotation.Y;
		float z2 = rotation.Z + rotation.Z;

		float a = (1f - rotation.Y * y2) - rotation.Z * z2;
		float b = rotation.X * y2 - rotation.W * z2;
		float c = rotation.X * z2 + rotation.W * y2;
		float d = rotation.X * y2 + rotation.W * z2;
		float e = (1f - rotation.X * x2) - rotation.Z * z2;
		float f = rotation.Y * z2 - rotation.W * x2;
		float g = rotation.X * z2 - rotation.W * y2;
		float h = rotation.Y * z2 + rotation.W * x2;
		float i = (1f - rotation.X * x2) - rotation.Y * y2;

		return new Matrix4(((value.M11 * a) + (value.M12 * b)) + (value.M13 * c), ((value.M11 * d) + (value.M12 * e))
				+ (value.M13 * f), ((value.M11 * g) + (value.M12 * h)) + (value.M13 * i), value.M14,

		((value.M21 * a) + (value.M22 * b)) + (value.M23 * c), ((value.M21 * d) + (value.M22 * e)) + (value.M23 * f),
				((value.M21 * g) + (value.M22 * h)) + (value.M23 * i), value.M24,

				((value.M31 * a) + (value.M32 * b)) + (value.M33 * c), ((value.M31 * d) + (value.M32 * e))
						+ (value.M33 * f), ((value.M31 * g) + (value.M32 * h)) + (value.M33 * i), value.M34,

				((value.M41 * a) + (value.M42 * b)) + (value.M43 * c), ((value.M41 * d) + (value.M42 * e))
						+ (value.M43 * f), ((value.M41 * g) + (value.M42 * h)) + (value.M43 * i), value.M44);
	}

	public static Matrix4 Transpose(Matrix4 matrix)
	{
		return new Matrix4(matrix.M11, matrix.M21, matrix.M31, matrix.M41, matrix.M12, matrix.M22, matrix.M32,
				matrix.M42, matrix.M13, matrix.M23, matrix.M33, matrix.M43, matrix.M14, matrix.M24, matrix.M34,
				matrix.M44);
	}

	public static Matrix4 Inverse3x3(Matrix4 matrix) throws Exception
	{
		if (matrix.Determinant3x3() == 0f)
		{
			throw new Exception("Singular matrix inverse not possible");
		}
		return (Matrix4.divide(Adjoint3x3(matrix), matrix.Determinant3x3()));
	}

	public static Matrix4 Adjoint3x3(Matrix4 matrix)
	{
		Matrix4 adjointMatrix = new Matrix4();
		for (int i = 0; i < 4; i++)
		{
			for (int j = 0; j < 4; j++)
			{
				adjointMatrix.setItem(i, j, (float) (Math.pow(-1, i + j) * (Minor(matrix, i, j).Determinant3x3())));
			}
		}
		adjointMatrix = Transpose(adjointMatrix);
		return adjointMatrix;
	}

	public static Matrix4 Inverse(Matrix4 matrix) throws Exception
	{
		if (matrix.Determinant() == 0f)
		{
			throw new Exception("Singular matrix inverse not possible");
		}
		return (Matrix4.divide(Adjoint(matrix), matrix.Determinant()));
	}

	public static Matrix4 Adjoint(Matrix4 matrix)
	{
		Matrix4 adjointMatrix = new Matrix4();
		for (int i = 0; i < 4; i++)
		{
			for (int j = 0; j < 4; j++)
			{
				adjointMatrix.setItem(i, j, (float) (Math.pow(-1, i + j) * ((Minor(matrix, i, j)).Determinant())));
			}
		}
		adjointMatrix = Transpose(adjointMatrix);
		return adjointMatrix;
	}

	public static Matrix4 Minor(Matrix4 matrix, int row, int col)
	{
		Matrix4 minor = new Matrix4();
		int m = 0, n = 0;

		for (int i = 0; i < 4; i++)
		{
			if (i == row)
			{
				continue;
			}
			n = 0;
			for (int j = 0; j < 4; j++)
			{
				if (j == col)
				{
					continue;
				}
				minor.setItem(m, n, matrix.getItem(i, j));
				n++;
			}
			m++;
		}

		return minor;
	}

	@Override
	public boolean equals(Object obj)
	{
		return (obj instanceof Matrix4) ? equals((Matrix4) obj) : false;
	}

	public boolean equals(Matrix4 other)
	{
		return (M11 == other.M11 && M12 == other.M12 && M13 == other.M13 && M14 == other.M14 && M21 == other.M21
				&& M22 == other.M22 && M23 == other.M23 && M24 == other.M24 && M31 == other.M31 && M32 == other.M32
				&& M33 == other.M33 && M34 == other.M34 && M41 == other.M41 && M42 == other.M42 && M43 == other.M43 && M44 == other.M44);
	}

	@Override
	public int hashCode()
	{
		return ((Float) M11).hashCode() ^ ((Float) M12).hashCode() ^ ((Float) M13).hashCode()
				^ ((Float) M14).hashCode() ^ ((Float) M21).hashCode() ^ ((Float) M22).hashCode()
				^ ((Float) M23).hashCode() ^ ((Float) M24).hashCode() ^ ((Float) M31).hashCode()
				^ ((Float) M32).hashCode() ^ ((Float) M33).hashCode() ^ ((Float) M34).hashCode()
				^ ((Float) M41).hashCode() ^ ((Float) M42).hashCode() ^ ((Float) M43).hashCode()
				^ ((Float) M44).hashCode();
	}

	/**
	 * Get a formatted string representation of the vector
	 * 
	 * @return A string representation of the vector
	 */

	@Override
	public String toString()
	{
		return String.format(Helpers.EnUsCulture,
				"|%f, %f, %f, %f|\n|%f, %f, %f, %f|\n|%f, %f, %f, %f|\n|%f, %f, %f, %f|", M11, M12, M13, M14, M21, M22,
				M23, M24, M31, M32, M33, M34, M41, M42, M43, M44);
	}

	public Vector4 getItem(int row) throws IndexOutOfBoundsException
	{
		switch (row)
		{
			case 0:
				return new Vector4(M11, M12, M13, M14);
			case 1:
				return new Vector4(M21, M22, M23, M24);
			case 2:
				return new Vector4(M31, M32, M33, M34);
			case 3:
				return new Vector4(M41, M42, M43, M44);
			default:
				throw new IndexOutOfBoundsException("Matrix4 row index must be from 0-3");
		}
	}

	public void setItem(int row, Vector4 value) throws IndexOutOfBoundsException
	{
		switch (row)
		{
			case 0:
				M11 = value.X;
				M12 = value.Y;
				M13 = value.Z;
				M14 = value.S;
				break;
			case 1:
				M21 = value.X;
				M22 = value.Y;
				M23 = value.Z;
				M24 = value.S;
				break;
			case 2:
				M31 = value.X;
				M32 = value.Y;
				M33 = value.Z;
				M34 = value.S;
				break;
			case 3:
				M41 = value.X;
				M42 = value.Y;
				M43 = value.Z;
				M44 = value.S;
				break;
			default:
				throw new IndexOutOfBoundsException("Matrix4 row index must be from 0-3");
		}
	}

	public float getItem(int row, int column) throws IndexOutOfBoundsException
	{
		switch (row)
		{
			case 0:
				switch (column)
				{
					case 0:
						return M11;
					case 1:
						return M12;
					case 2:
						return M13;
					case 3:
						return M14;
					default:
						throw new IndexOutOfBoundsException("Matrix4 row and column values must be from 0-3");
				}
			case 1:
				switch (column)
				{
					case 0:
						return M21;
					case 1:
						return M22;
					case 2:
						return M23;
					case 3:
						return M24;
					default:
						throw new IndexOutOfBoundsException("Matrix4 row and column values must be from 0-3");
				}
			case 2:
				switch (column)
				{
					case 0:
						return M31;
					case 1:
						return M32;
					case 2:
						return M33;
					case 3:
						return M34;
					default:
						throw new IndexOutOfBoundsException("Matrix4 row and column values must be from 0-3");
				}
			case 3:
				switch (column)
				{
					case 0:
						return M41;
					case 1:
						return M42;
					case 2:
						return M43;
					case 3:
						return M44;
					default:
						throw new IndexOutOfBoundsException("Matrix4 row and column values must be from 0-3");
				}
			default:
				throw new IndexOutOfBoundsException("Matrix4 row and column values must be from 0-3");
		}
	}

	public void setItem(int row, int column, float value)
	{
		switch (row)
		{
			case 0:
				switch (column)
				{
					case 0:
						M11 = value;
						return;
					case 1:
						M12 = value;
						return;
					case 2:
						M13 = value;
						return;
					case 3:
						M14 = value;
						return;
					default:
						throw new IndexOutOfBoundsException("Matrix4 row and column values must be from 0-3");
				}
			case 1:
				switch (column)
				{
					case 0:
						M21 = value;
						return;
					case 1:
						M22 = value;
						return;
					case 2:
						M23 = value;
						return;
					case 3:
						M24 = value;
						return;
					default:
						throw new IndexOutOfBoundsException("Matrix4 row and column values must be from 0-3");
				}
			case 2:
				switch (column)
				{
					case 0:
						M31 = value;
						return;
					case 1:
						M32 = value;
						return;
					case 2:
						M33 = value;
						return;
					case 3:
						M34 = value;
						return;
					default:
						throw new IndexOutOfBoundsException("Matrix4 row and column values must be from 0-3");
				}
			case 3:
				switch (column)
				{
					case 0:
						M41 = value;
						return;
					case 1:
						M42 = value;
						return;
					case 2:
						M43 = value;
						return;
					case 3:
						M44 = value;
						return;
					default:
						throw new IndexOutOfBoundsException("Matrix4 row and column values must be from 0-3");
				}
			default:
				throw new IndexOutOfBoundsException("Matrix4 row and column values must be from 0-3");
		}
	}

	/** A 4x4 matrix containing all zeroes */
	public static final Matrix4 Zero = new Matrix4();

	/** A 4x4 identity matrix */
	public static final Matrix4 Identity = new Matrix4(1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f);
}
