package aegismatrix.com.namm_radio;

import android.graphics.Bitmap;
import android.media.effect.Effect;
import android.media.effect.EffectContext;
import android.media.effect.EffectFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Dhiraj on 06-07-2017.
 */

public class NammRadioGLViewSurface implements GLSurfaceView.Renderer {

    private int[] mTextures = new int[2];
    private Bitmap mBitmap;
    private int mImageWidth;
    private TextureRenderer mTexRenderer = new TextureRenderer();
    private int mImageHeight;
    private EffectContext mEffectContext;
    private int mCurrentEffect;
    private boolean mInitialized;
    private Effect mEffect;

    public NammRadioGLViewSurface(Bitmap bitmap) {
        this.mBitmap = bitmap;
        mCurrentEffect = R.id.none;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        if (mTexRenderer != null) {
            mTexRenderer.updateViewSize(width, height);
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (!mInitialized) {
            //Only need to do this once  
            mEffectContext = EffectContext.createWithCurrentGlContext();
            mTexRenderer.init();
            loadTextures();
            mInitialized = true;
        }
        if (mCurrentEffect != R.id.none) {
            //if an effect is chosen initialize it and apply it to the texture  
            initEffect();
            applyEffect();
        }
        renderResult();
    }

    private void renderResult() {
        if (mCurrentEffect != R.id.none) {
            // if no effect is chosen, just render the original bitmap
            mTexRenderer.renderTexture(mTextures[1]);
        } else {
            // render the result of applyEffect()
            mTexRenderer.renderTexture(mTextures[0]);
        }
    }

    private void applyEffect() {
        mEffect.apply(mTextures[0], mImageWidth, mImageHeight, mTextures[1]);
    }

    private void initEffect() {
        EffectFactory effectFactory = mEffectContext.getFactory();
        if (mEffect != null) {
            mEffect.release();
        }
        /**
         * Initialize the correct effect based on the selected menu/action item  
         */
        if (mCurrentEffect != R.id.none) {
            mEffect = effectFactory.createEffect(EffectFactory.EFFECT_GRAIN);
            mEffect.setParameter("strength", 1.0f);
        }
    }

    private void loadTextures() {
        // Generate textures
        GLES20.glGenTextures(2, mTextures, 0);
        // Load input bitmap
        mImageWidth = mBitmap.getWidth();
        mImageHeight = mBitmap.getHeight();
        mTexRenderer.updateTextureSize(mImageWidth, mImageHeight);
        // Upload to texture
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures[0]);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mBitmap, 0);
        // Set texture parameters
        GLToolbox.initTexParams();
    }

    private class TextureRenderer {
        private int mProgram;
        private int mTexSamplerHandle;
        private int mTexCoordHandle;
        private int mPosCoordHandle;
        private FloatBuffer mTexVertices;
        private FloatBuffer mPosVertices;
        private int mViewWidth;
        private int mViewHeight;
        private int mTexWidth;
        private int mTexHeight;
        private static final String VERTEX_SHADER =
                "attribute vec4 a_position;\n" +
                        "attribute vec2 a_texcoord;\n" +
                        "varying vec2 v_texcoord;\n" +
                        "void main() {\n" +
                        " gl_Position = a_position;\n" +
                        " v_texcoord = a_texcoord;\n" +
                        "}\n";
        private static final String FRAGMENT_SHADER =
                "precision mediump float;\n" +
                        "uniform sampler2D tex_sampler;\n" +
                        "varying vec2 v_texcoord;\n" +
                        "void main() {\n" +
                        " gl_FragColor = texture2D(tex_sampler, v_texcoord);\n" +
                        "}\n";
        private final float[] TEX_VERTICES = {
                0.0f, 1.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f
        };
        private final float[] POS_VERTICES = {
                -1.0f, -1.0f, 1.0f, -1.0f, -1.0f, 1.0f, 1.0f, 1.0f
        };
        private static final int FLOAT_SIZE_BYTES = 4;

        public void init() {
            // Create program
            mProgram = GLToolbox.createProgram(VERTEX_SHADER, FRAGMENT_SHADER);
            // Bind attributes and uniforms
            mTexSamplerHandle = GLES20.glGetUniformLocation(mProgram,
                    "tex_sampler");
            mTexCoordHandle = GLES20.glGetAttribLocation(mProgram, "a_texcoord");
            mPosCoordHandle = GLES20.glGetAttribLocation(mProgram, "a_position");
            // Setup coordinate buffers
            mTexVertices = ByteBuffer.allocateDirect(
                    TEX_VERTICES.length * FLOAT_SIZE_BYTES)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            mTexVertices.put(TEX_VERTICES).position(0);
            mPosVertices = ByteBuffer.allocateDirect(
                    POS_VERTICES.length * FLOAT_SIZE_BYTES)
                    .order(ByteOrder.nativeOrder()).asFloatBuffer();
            mPosVertices.put(POS_VERTICES).position(0);
        }

        public void tearDown() {
            GLES20.glDeleteProgram(mProgram);
        }

        public void updateTextureSize(int texWidth, int texHeight) {
            mTexWidth = texWidth;
            mTexHeight = texHeight;
            computeOutputVertices();
        }

        public void updateViewSize(int viewWidth, int viewHeight) {
            mViewWidth = viewWidth;
            mViewHeight = viewHeight;
            computeOutputVertices();
        }

        public void renderTexture(int texId) {
            // Bind default FBO
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
            // Use our shader program
            GLES20.glUseProgram(mProgram);
            GLToolbox.checkGlError("glUseProgram");
            // Set viewport
            GLES20.glViewport(0, 0, mViewWidth, mViewHeight);
            GLToolbox.checkGlError("glViewport");
            // Disable blending
            GLES20.glDisable(GLES20.GL_BLEND);
            // Set the vertex attributes
            GLES20.glVertexAttribPointer(mTexCoordHandle, 2, GLES20.GL_FLOAT, false,
                    0, mTexVertices);
            GLES20.glEnableVertexAttribArray(mTexCoordHandle);
            GLES20.glVertexAttribPointer(mPosCoordHandle, 2, GLES20.GL_FLOAT, false,
                    0, mPosVertices);
            GLES20.glEnableVertexAttribArray(mPosCoordHandle);
            GLToolbox.checkGlError("vertex attribute setup");
            // Set the input texture
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLToolbox.checkGlError("glActiveTexture");
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId);
            GLToolbox.checkGlError("glBindTexture");
            GLES20.glUniform1i(mTexSamplerHandle, 0);
            // Draw
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        }

        private void computeOutputVertices() {
            if (mPosVertices != null) {
                float imgAspectRatio = mTexWidth / (float) mTexHeight;
                float viewAspectRatio = mViewWidth / (float) mViewHeight;
                float relativeAspectRatio = viewAspectRatio / imgAspectRatio;
                float x0, y0, x1, y1;
                if (relativeAspectRatio > 1.0f) {
                    x0 = -1.0f / relativeAspectRatio;
                    y0 = -1.0f;
                    x1 = 1.0f / relativeAspectRatio;
                    y1 = 1.0f;
                } else {
                    x0 = -1.0f;
                    y0 = -relativeAspectRatio;
                    x1 = 1.0f;
                    y1 = relativeAspectRatio;
                }
                float[] coords = new float[]{x0, y0, x1, y0, x0, y1, x1, y1};
                mPosVertices.put(coords).position(0);
            }
        }
    }

    private static class GLToolbox {
        public static int loadShader(int shaderType, String source) {
            int shader = GLES20.glCreateShader(shaderType);
            if (shader != 0) {
                GLES20.glShaderSource(shader, source);
                GLES20.glCompileShader(shader);
                int[] compiled = new int[1];
                GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
                if (compiled[0] == 0) {
                    String info = GLES20.glGetShaderInfoLog(shader);
                    GLES20.glDeleteShader(shader);
                    shader = 0;
                    throw new RuntimeException("Could not compile shader " +
                            shaderType + ":" + info);
                }
            }
            return shader;
        }

        public static int createProgram(String vertexSource,
                                        String fragmentSource) {
            int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
            if (vertexShader == 0) {
                return 0;
            }
            int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
            if (pixelShader == 0) {
                return 0;
            }
            int program = GLES20.glCreateProgram();
            if (program != 0) {
                GLES20.glAttachShader(program, vertexShader);
                checkGlError("glAttachShader");
                GLES20.glAttachShader(program, pixelShader);
                checkGlError("glAttachShader");
                GLES20.glLinkProgram(program);
                int[] linkStatus = new int[1];
                GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus,
                        0);
                if (linkStatus[0] != GLES20.GL_TRUE) {
                    String info = GLES20.glGetProgramInfoLog(program);
                    GLES20.glDeleteProgram(program);
                    program = 0;
                    throw new RuntimeException("Could not link program: " + info);
                }
            }
            return program;
        }

        public static void checkGlError(String op) {
            int error;
            while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
                throw new RuntimeException(op + ": glError " + error);
            }
        }

        public static void initTexParams() {
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D,
                    GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                    GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                    GLES20.GL_CLAMP_TO_EDGE);
        }
    }
}