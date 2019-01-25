/*
 * Copyright (C) 2012 CyberAgent
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.emptech.biocollectiononline.camera.utils;

public class TextureRotationUtil {

    public static final float TEXTURE_NO_ROTATION[] = {
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,
    };

    public static final float TEXTURE_ROTATED_90[] = {
            1.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            0.0f, 0.0f,
    };
    public static final float TEXTURE_ROTATED_180[] = {
            1.0f, 0.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,
    };
    public static final float TEXTURE_ROTATED_270[] = {
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
    };
    
//    public static final float CUBE[] = {
//        -1.0f, -1.0f,
//        1.0f, -1.0f,
//        -1.0f, 1.0f,
//        1.0f, 1.0f,
//	};
    public static final float CUBE[] = {
        -2f, -2f,
        2f, -2f,
        -2f, 2f,
        2f, 2f,
	};
    
    private TextureRotationUtil() {}

    public static float[] getRotation(final Rotation rotation, final boolean flipHorizontal,
                                                         final boolean flipVertical) {
        float[] rotatedTex;
        switch (rotation) {
            case ROTATION_90:
                rotatedTex = TEXTURE_ROTATED_90;
                break;
            case ROTATION_180:
                rotatedTex = TEXTURE_ROTATED_180;
                break;
            case ROTATION_270:
                rotatedTex = TEXTURE_ROTATED_270;
                break;
            case NORMAL:
            default:
                rotatedTex = TEXTURE_NO_ROTATION;
                break;
        }
        if (flipHorizontal) {
            rotatedTex = new float[]{
                    flip(rotatedTex[0]), rotatedTex[1],
                    flip(rotatedTex[2]), rotatedTex[3],
                    flip(rotatedTex[4]), rotatedTex[5],
                    flip(rotatedTex[6]), rotatedTex[7],
            };
        }
        if (flipVertical) {
            rotatedTex = new float[]{
                    rotatedTex[0], flip(rotatedTex[1]),
                    rotatedTex[2], flip(rotatedTex[3]),
                    rotatedTex[4], flip(rotatedTex[5]),
                    rotatedTex[6], flip(rotatedTex[7]),
            };
        }
        return rotatedTex;
    }

    public static float[] getRotation(final Rotation rotation, final boolean flipHorizontal,
                                                         final boolean flipVertical,int srcWidth,int srcHeight,int dstWidth,int dstHeight) {
        float[] rotatedTex;
        switch (rotation) {
            case ROTATION_90:
                rotatedTex = TEXTURE_ROTATED_90;
                break;
            case ROTATION_180:
                rotatedTex = TEXTURE_ROTATED_180;
                break;
            case ROTATION_270:
                rotatedTex = TEXTURE_ROTATED_270;
                break;
            case NORMAL:
            default:
                rotatedTex = TEXTURE_NO_ROTATION;
                break;
        }
        if (flipHorizontal) {
            rotatedTex = new float[]{
                    flip(rotatedTex[0]), rotatedTex[1],
                    flip(rotatedTex[2]), rotatedTex[3],
                    flip(rotatedTex[4]), rotatedTex[5],
                    flip(rotatedTex[6]), rotatedTex[7],
            };
        }
        if (flipVertical) {
            rotatedTex = new float[]{
                    rotatedTex[0], flip(rotatedTex[1]),
                    rotatedTex[2], flip(rotatedTex[3]),
                    rotatedTex[4], flip(rotatedTex[5]),
                    rotatedTex[6], flip(rotatedTex[7]),
            };
        }
        float leftOffset=(srcWidth-dstWidth)*1.0f/(2*srcWidth);
        float topOffset=(srcHeight-dstHeight)*1.0f/(2*srcHeight);
        float widthp=dstWidth*1.0f/srcWidth;
        float heightp=dstHeight*1.0f/srcHeight;

        rotatedTex = new float[]{
                rotatedTex[0]==0?+leftOffset:-leftOffset,rotatedTex[1]==0?+topOffset:-topOffset,
                rotatedTex[2]==0?+leftOffset:-leftOffset, rotatedTex[3]==0?+topOffset:-topOffset,
                rotatedTex[4]==0?+leftOffset:-leftOffset, rotatedTex[5]==0?+topOffset:-topOffset,
                rotatedTex[6]==0?+leftOffset:-leftOffset, rotatedTex[7]==0?+topOffset:-topOffset,
        };
        return rotatedTex;
    }


    private static float flip(final float i) {
        if (i == 0.0f) {
            return 1.0f;
        }
        return 0.0f;
    }

    public static float[] scaleVertex(float[] vertices, float v) {
        return new float[]{
              vertices[0]*v,vertices[1]*v,
              vertices[2]*v,vertices[3]*v,
              vertices[4]*v,vertices[5]*v,
              vertices[6]*v,vertices[7]*v
        };
    }
}
