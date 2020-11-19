package main.reseni;

import lwjglutils.*;
import main.AbstractRenderer;
import main.GridFactory;
import main.LwjglWindow;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import transforms.*;

import java.io.IOException;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;

public class RendererTwoObjectsReflector extends AbstractRenderer {

    private int shaderProgram1, shaderProgram2, shaderProgramGrid , shaderProgramElephant,shaderProgramDuck;
    private OGLBuffers buffersDuck,buffersElephant ,buffersSun;

    private double otoceni =0.01;
    private int locView, locProjection, locTemp, locLightPos;
    private int locView2, locProjection2;
    private int locViewGrid, locProjectionGrid;
    //private int locViewElephant, locProjectionElephant ,locLightPosElephant,locCameraPosElephant;
    private int locViewDuck, locProjectionDuck ,locLightPosDuck,locCameraPosDuck;
    private int locViewSun, locProjectionSun, shaderProgramSun, locLightPosSun,locLightDirSun, locLightSpotCutOffSun;
    private int locViewObjekt, locProjectionObjekt,locCameraPosObjekt,locLightPosObjekt,locTempObjekt,locLightDirObjekt,locLightSpotCutOffObjekt ,locLightObjekt,shaderProgramObjekt,locCameraObjekt;
    private Camera camera;
    private Camera cameraLight;
    private Mat4 projection;
    private boolean per;
    OGLModelOBJ modelElephant,modelDuck;
    private OGLTexture2D texture1;
    private OGLTexture2D.Viewer viewer;
    private OGLRenderTarget renderTarget;
    private Vec3D lightDir = new Vec3D(1,0,0);
    private float lightSpotCutOff = (float)Math.PI/(float)4;

    @Override
    public void init() {
        OGLUtils.printOGLparameters();
        OGLUtils.printLWJLparameters();
        OGLUtils.printJAVAparameters();
        OGLUtils.shaderCheck();

        textRenderer = new OGLTextRenderer(LwjglWindow.WIDTH, LwjglWindow.HEIGHT);

        shaderProgramObjekt = ShaderUtils.loadProgram("/refrector/objekt");
        //shaderProgramDuck = ShaderUtils.loadProgram("/refrector/duck");
        shaderProgramSun = ShaderUtils.loadProgram("/refrector/objSun");

        locViewObjekt = glGetUniformLocation(shaderProgramObjekt, "view");
        locProjectionObjekt = glGetUniformLocation(shaderProgramObjekt, "projection");
        locLightPosObjekt = glGetUniformLocation(shaderProgramObjekt, "lightPos");
        locCameraPosObjekt = glGetUniformLocation(shaderProgramObjekt, "cameraPos");
        locTempObjekt = glGetUniformLocation(shaderProgramObjekt, "temp");
        locLightDirObjekt = glGetUniformLocation(shaderProgramObjekt, "lightDir");
        locLightSpotCutOffObjekt = glGetUniformLocation(shaderProgramObjekt, "lightSpotCutOff");

        //locViewDuck = glGetUniformLocation(shaderProgramDuck, "view");
        //locProjectionDuck = glGetUniformLocation(shaderProgramDuck, "projection");
        //locLightPosDuck = glGetUniformLocation(shaderProgramDuck, "lightPos");
        //locCameraPosDuck = glGetUniformLocation(shaderProgramDuck, "cameraPos");

        locViewSun = glGetUniformLocation(shaderProgramSun, "view");
        locProjectionSun = glGetUniformLocation(shaderProgramSun, "projection");
        locLightPosSun = glGetUniformLocation(shaderProgramSun, "lightPos");
        locLightDirSun = glGetUniformLocation(shaderProgramSun, "lightDir");
        locLightSpotCutOffSun = glGetUniformLocation(shaderProgramSun, "lightSpotCutOff");


        modelElephant = new OGLModelOBJ("/obj/ElephantBody.obj");
        buffersElephant = modelElephant.getBuffers();

        modelDuck= new OGLModelOBJ("/obj/ducky.obj");
        buffersDuck = modelDuck.getBuffers();

//*

        buffersSun = GridFactory.generateGridStrip(100, 100);
/*/
        buffers = GridFactory.generateGrid(4,4);
        //*/
//        camera = new Camera(
//                new Vec3D(6, 6, 5),
//                5 / 4f * Math.PI,
//                -1 / 5f * Math.PI,
//                1,
//                true
//        );
        camera = new Camera()
                .withPosition(new Vec3D(2, 2, 1)) // pozice pozorovatele
                .withAzimuth(5 / 4f * Math.PI) // otočení do strany o (180+45) stupňů v radiánech
                .withZenith(-1 / 5f * Math.PI); // otočení (90/5) stupňů dolů

        cameraLight = new Camera().withPosition(new Vec3D(-2, -2, 1));

//        view = new Mat4ViewRH(
//                new Vec3D(4, 4, 4),
//                new Vec3D(-1, -1, -1),
//                new Vec3D(0, 0, 1)
//        );

        projection = new Mat4PerspRH(Math.PI / 3, LwjglWindow.HEIGHT / (float)LwjglWindow.WIDTH, 1, 20);
//        projection = new Mat4OrthoRH(10, 7, 1, 20);

        try {
            texture1 = new OGLTexture2D("./textures/testTexture.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }

        viewer = new OGLTexture2D.Viewer();

        renderTarget = new OGLRenderTarget(1024, 1024);
    }

    @Override
    public void display() {
        glEnable(GL_DEPTH_TEST);
        perspective();
        clearAndViewPort();

        renderObjekt();
        //renderDuck();
        renderSunPos();

        cameraLight = cameraLight.withPosition(new Vec3D(
                cameraLight.getPosition().getX()*Math.cos(otoceni)-cameraLight.getPosition().getY()*Math.sin(otoceni),
                cameraLight.getPosition().getX()*Math.sin(otoceni)+cameraLight.getPosition().getY()*Math.cos(otoceni),
                cameraLight.getPosition().getZ()
        ));

        textRenderer.addStr2D(LwjglWindow.WIDTH - 500, LwjglWindow.HEIGHT - 3, camera.getPosition().toString() + " azimut: "+ camera.getAzimuth() + " zenit: "+ camera.getZenith());
    }

    private void renderObjekt(){
        glUseProgram(shaderProgramObjekt);

        //renderTarget.bind();
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

        glUniform3fv(locLightPosObjekt, ToFloatArray.convert(cameraLight.getPosition()));
        glUniform3fv(locLightDirObjekt, ToFloatArray.convert(lightDir));

        glUniform3fv(locLightPosObjekt, ToFloatArray.convert(cameraLight.getPosition()));
        glUniform3fv(locCameraPosObjekt, ToFloatArray.convert(camera.getPosition()));
        glUniformMatrix4fv(locViewObjekt, false, camera.getViewMatrix().floatArray());
        glUniformMatrix4fv(locProjectionObjekt, false, projection.floatArray());

        glUniform1f(locTempObjekt, 0.0f);
        buffersElephant.draw(modelElephant.getTopology(), shaderProgramObjekt);
        glUniform1f(locTempObjekt, 1.0f);
        buffersDuck.draw(modelDuck.getTopology(), shaderProgramObjekt);
    }

    private void renderDuck(){
        glUseProgram(shaderProgramDuck);

        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

        glUniform3fv(locLightPosDuck, ToFloatArray.convert(cameraLight.getPosition()));
        glUniform3fv(locCameraPosDuck, ToFloatArray.convert(camera.getPosition()));
        glUniformMatrix4fv(locViewDuck, false, camera.getViewMatrix().floatArray());
        glUniformMatrix4fv(locProjectionDuck, false, projection.floatArray());

        //texture1.bind(shaderProgramGrid, "texture1", 0);

    }

    private void renderSunPos(){
        glUseProgram(shaderProgramSun);

        glUniform3fv(locLightPosSun, ToFloatArray.convert(cameraLight.getPosition()));
        glUniform3fv(locLightDirSun, ToFloatArray.convert(lightDir));
        glUniform1f(locLightSpotCutOffSun, lightSpotCutOff);
        glUniformMatrix4fv(locViewSun, false, camera.getViewMatrix().floatArray());
        glUniformMatrix4fv(locProjectionSun, false, projection.floatArray());

        //texture1.bind(shaderProgramGrid, "texture1", 0);
        buffersSun.draw(GL_TRIANGLE_STRIP, shaderProgramSun);
    }

    public void clearAndViewPort(){
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        glClearColor(0, .1f, 0, 1);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glViewport(0, 0, LwjglWindow.WIDTH, LwjglWindow.HEIGHT);
    }
    public void perspective(){
        if(!per){
            projection = new Mat4PerspRH(Math.PI / 3, LwjglWindow.HEIGHT / (float)LwjglWindow.WIDTH, 1, 20);
        }else {
            projection = new Mat4OrthoRH(
                    20*LwjglWindow.WIDTH / (float)LwjglWindow.HEIGHT,
                    20*LwjglWindow.WIDTH / (float)LwjglWindow.HEIGHT,
                    1,20);
        }
    }

    @Override
    public GLFWWindowSizeCallback getWsCallback() {
        return windowResCallback; // FIXME
    }

    @Override
    public GLFWCursorPosCallback getCursorCallback() {
        return cursorPosCallback;
    }

    @Override
    public GLFWMouseButtonCallback getMouseCallback() {
        return mouseButtonCallback;
    }

    @Override
    public GLFWKeyCallback getKeyCallback() {
        return keyCallback;
    }

    private double oldMx, oldMy;
    private boolean mousePressed;

    private final GLFWWindowSizeCallback windowResCallback = new GLFWWindowSizeCallback() {
        @Override
        public void invoke(long window, int width, int height) {
            LwjglWindow.HEIGHT = height;
            LwjglWindow.WIDTH = width;
        }
    };

    private final GLFWCursorPosCallback cursorPosCallback = new GLFWCursorPosCallback() {
        @Override
        public void invoke(long window, double x, double y) {
            if (mousePressed) {
                camera = camera.addAzimuth(Math.PI * (oldMx - x) / LwjglWindow.WIDTH);
                camera = camera.addZenith(Math.PI * (y - oldMy) / LwjglWindow.HEIGHT);
                oldMx = x;
                oldMy = y;
            }
        }
    };

    private final GLFWMouseButtonCallback mouseButtonCallback = new GLFWMouseButtonCallback() {
        @Override
        public void invoke(long window, int button, int action, int mods) {
            if (button == GLFW_MOUSE_BUTTON_LEFT) {
                double[] xPos = new double[1];
                double[] yPos = new double[1];
                glfwGetCursorPos(window, xPos, yPos);
                oldMx = xPos[0];
                oldMy = yPos[0];
                mousePressed = action == GLFW_PRESS;
            }
        }
    };

    private final GLFWKeyCallback keyCallback = new GLFWKeyCallback() {
        @Override
        public void invoke(long window, int key, int scancode, int action, int mods) {
            if (action == GLFW_PRESS || action == GLFW_REPEAT) {
                switch (key) {
                    case GLFW_KEY_A :
                        camera = camera.left(0.1);
                        break;
                    case GLFW_KEY_D :
                        camera = camera.right(0.1);
                        break;
                    case GLFW_KEY_W :
                        camera = camera.forward(0.1);
                        break;
                    case GLFW_KEY_S :
                        camera = camera.backward(0.1);
                        break;
                    case GLFW_KEY_R :
                        camera = camera.up(0.1);
                        break;
                    case GLFW_KEY_F :
                        camera = camera.down(0.1);
                        break;
                    case GLFW_KEY_P :
                        per=!per;
                        break;
                }
            }
        }
    };
    public static void main(String[] args) {
        new LwjglWindow(new RendererTwoObjectsReflector());
    }

}
