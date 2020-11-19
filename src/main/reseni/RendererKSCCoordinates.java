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

public class RendererKSCCoordinates extends AbstractRenderer {

    private int shaderProgram1, shaderProgram2, shaderProgramKartez1,shaderProgramValec1,shaderProgramKoule1, shaderProgramElephant;
    private OGLBuffers buffers,buffersElephant ,buffersSun;

    private double otoceni =0.01;
    private float varObj = 0;
    private int locView, locProjection, locTemp, locLightPos;
    private int locView2, locProjection2;
    private int locViewKartez1, locProjectionKartez1, locObjCountKartez1, locVarObjKartez1;
    private int locViewValec1, locProjectionValec1, locObjCountValec1, locVarObjValec1;
    private int locViewKoule1, locProjectionKoule1, locObjCountKoule1, locVarObjKoule1;
    private int locViewElephant, locProjectionElephant ,locLightPosElephant,shaderProgramElephantSun;
    private int locViewElephantSun, locProjectionElephantSun ,locLightPosElephantSun;
    private int locViewObjekt, locProjectionObjekt ,locLightObjekt,shaderProgramObjekt,locCameraObjekt;
    private Camera camera;
    private Camera cameraLight;
    private Mat4 projection;
    private boolean per;
    OGLModelOBJ modelElephant;
    private OGLTexture2D texture1;
    private OGLTexture2D.Viewer viewer;
    private OGLRenderTarget renderTarget;

    @Override
    public void init() {
        OGLUtils.printOGLparameters();
        OGLUtils.printLWJLparameters();
        OGLUtils.printJAVAparameters();
        OGLUtils.shaderCheck();

        textRenderer = new OGLTextRenderer(LwjglWindow.WIDTH, LwjglWindow.HEIGHT);

        shaderProgram1 = ShaderUtils.loadProgram("/start");
        shaderProgram2 = ShaderUtils.loadProgram("/postProc");
        shaderProgramKartez1 = ShaderUtils.loadProgram("/KSC/kartez");
        shaderProgramKoule1 = ShaderUtils.loadProgram("/KSC/koule");
        shaderProgramValec1 = ShaderUtils.loadProgram("/KSC/valec");

        locViewKartez1 = glGetUniformLocation(shaderProgramKartez1, "view");
        locProjectionKartez1 = glGetUniformLocation(shaderProgramKartez1, "projection");
        locObjCountKartez1 = glGetUniformLocation(shaderProgramKartez1, "objCount");
        locVarObjKartez1 = glGetUniformLocation(shaderProgramKartez1, "varObj");

        locViewKoule1 = glGetUniformLocation(shaderProgramKoule1, "view");
        locProjectionKoule1 = glGetUniformLocation(shaderProgramKoule1, "projection");
        locObjCountKoule1 = glGetUniformLocation(shaderProgramKoule1, "objCount");
        locVarObjKoule1 = glGetUniformLocation(shaderProgramKoule1, "varObj");

        locViewValec1 = glGetUniformLocation(shaderProgramValec1, "view");
        locProjectionValec1 = glGetUniformLocation(shaderProgramValec1, "projection");
        locObjCountValec1 = glGetUniformLocation(shaderProgramValec1, "objCount");
        locVarObjValec1 = glGetUniformLocation(shaderProgramValec1, "varObj");




        modelElephant = new OGLModelOBJ("/obj/ElephantBody.obj");
        buffersElephant = modelElephant.getBuffers();

//*
        buffers = GridFactory.generateGridStrip(100, 100);
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
        renderKartez();
       renderKoule();
       renderValec();
        //viewer.view(texture1, -1, -1, 0.5);
        //viewer.view(renderTarget.getColorTexture(), -1, -0.5, 0.5);
        cameraLight = cameraLight.withPosition(new Vec3D(
                cameraLight.getPosition().getX()*Math.cos(otoceni)-cameraLight.getPosition().getY()*Math.sin(otoceni),
                cameraLight.getPosition().getX()*Math.sin(otoceni)+cameraLight.getPosition().getY()*Math.cos(otoceni),
                cameraLight.getPosition().getZ()
        ));

        textRenderer.addStr2D(LwjglWindow.WIDTH - 500, LwjglWindow.HEIGHT - 3, camera.getPosition().toString() + " azimut: "+ camera.getAzimuth() + " zenit: "+ camera.getZenith());
    }
    
    private void renderKartez(){
        glUseProgram(shaderProgramKartez1);

        //renderTarget.bind();

        varObj +=0.01;
        glUniform1f(locObjCountKartez1, 1.0f);
        glUniform1f(locVarObjKartez1, varObj);
        glUniformMatrix4fv(locViewKartez1, false, camera.getViewMatrix().floatArray());
        glUniformMatrix4fv(locProjectionKartez1, false, projection.floatArray());

        //texture1.bind(shaderProgramCylindrick1, "texture1", 0);
        buffers.draw(GL_LINES, shaderProgramKartez1);

        glUniform1f(locObjCountKartez1, 0.0f);
        buffers.draw(GL_LINES, shaderProgramKartez1);
    }

    private void renderValec(){
        glUseProgram(shaderProgramValec1);

        //renderTarget.bind();


        glUniform1f(locObjCountValec1, 1.0f);
        glUniformMatrix4fv(locViewValec1, false, camera.getViewMatrix().floatArray());
        glUniformMatrix4fv(locProjectionValec1, false, projection.floatArray());

        //texture1.bind(shaderProgramCylindrick1, "texture1", 0);
        buffers.draw(GL_LINES, shaderProgramValec1);

        glUniform1f(locObjCountValec1, 0.0f);
        buffers.draw(GL_LINES, shaderProgramValec1);
    }

    private void renderKoule(){
        glUseProgram(shaderProgramKoule1);

        //renderTarget.bind();

        glUniform1f(locObjCountKoule1, 1.0f);
        glUniformMatrix4fv(locViewKoule1, false, camera.getViewMatrix().floatArray());
        glUniformMatrix4fv(locProjectionKoule1, false, projection.floatArray());

        //texture1.bind(shaderProgramCylindrick1, "texture1", 0);
        buffers.draw(GL_LINES, shaderProgramKoule1);

        glUniform1f(locObjCountKartez1, 0.0f);
        buffers.draw(GL_LINES, shaderProgramKoule1);
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
        new LwjglWindow(new RendererKSCCoordinates());
    }

}
