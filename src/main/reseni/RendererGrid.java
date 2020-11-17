package main.reseni;

import lwjglutils.*;
import main.AbstractRenderer;
import main.GridFactory;
import main.LwjglWindow;
import main.Renderer;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import transforms.Camera;
import transforms.Mat4;
import transforms.Mat4PerspRH;
import transforms.Vec3D;

import java.io.IOException;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;

public class RendererGrid extends AbstractRenderer {

    private int shaderProgram1, shaderProgram2, shaderProgramGrid , shaderProgramElephant;
    private OGLBuffers buffers , buffersStrip,buffersElephant ,buffersSun;

    private double otoceni =0.01;
    private int locView, locProjection, locTemp, locLightPos;
    private int locView2, locProjection2;
    private int locViewGrid, locProjectionGrid;
    private int locViewElephant, locProjectionElephant ,locLightPosElephant,shaderProgramElephantSun;
    private int locViewElephantSun, locProjectionElephantSun ,locLightPosElephantSun;
    private int locViewObjekt, locProjectionObjekt ,locLightObjekt,shaderProgramObjekt,locCameraObjekt;
    private Camera camera;
    private Camera cameraLight;
    private Mat4 projection;
    private int switchInt = 0;

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

        shaderProgramGrid = ShaderUtils.loadProgram("/grid/grid");

        locViewGrid = glGetUniformLocation(shaderProgramGrid, "view");
        locProjectionGrid = glGetUniformLocation(shaderProgramGrid, "projection");


        buffers = GridFactory.generateGrid(100, 100);
        buffersStrip = GridFactory.generateGridStrip(100, 100);

        camera = new Camera()
                .withPosition(new Vec3D(2, 2, 1)) // pozice pozorovatele
                .withAzimuth(5 / 4f * Math.PI) // otočení do strany o (180+45) stupňů v radiánech
                .withZenith(-1 / 5f * Math.PI); // otočení (90/5) stupňů dolů

        cameraLight = new Camera().withPosition(new Vec3D(-2, -2, 1));


        projection = new Mat4PerspRH(Math.PI / 3, LwjglWindow.HEIGHT / (float)LwjglWindow.WIDTH, 1, 20);

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

        gridRender();

        textRenderer.addStr2D(LwjglWindow.WIDTH - 500, LwjglWindow.HEIGHT - 3, camera.getPosition().toString() + " azimut: "+ camera.getAzimuth() + " zenit: "+ camera.getZenith());
    }

    private void gridRender(){
        glUseProgram(shaderProgramGrid);

        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        glClearColor(0, .1f, 0, 1);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glViewport(0, 0, LwjglWindow.WIDTH, LwjglWindow.HEIGHT);

        glUniformMatrix4fv(locViewGrid, false, camera.getViewMatrix().floatArray());
        glUniformMatrix4fv(locProjectionGrid, false, projection.floatArray());

        switch (switchInt){
            case 0:
                buffers.draw(GL_TRIANGLES, shaderProgramGrid);
                break;
            case 1:
                buffers.draw(GL_LINES, shaderProgramGrid);
                break;
            case 2:
                buffers.draw(GL_POINTS, shaderProgramGrid);
                break;
            case 3:
                buffersStrip.draw(GL_TRIANGLE_STRIP, shaderProgramGrid);
                break;
            case 4:
                buffersStrip.draw(GL_LINES, shaderProgramGrid);
                break;
            case 5:
                buffersStrip.draw(GL_POINTS, shaderProgramGrid);
                break;
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
                    case GLFW_KEY_M :
                        switchInt = (switchInt+1)%6;
                        break;
                }
            }
        }
    };

    public static void main(String[] args) {
        new LwjglWindow(new RendererGrid());
    }


}
