package main;

import lwjglutils.*;
import org.lwjgl.glfw.*;
import transforms.*;

import java.io.IOException;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Renderer extends AbstractRenderer {

    private int shaderProgram1, shaderProgram2, shaderProgramGrid , shaderProgramElephant;
    private OGLBuffers buffers,buffersElephant;

    private double otoceni =0;
    private int locView, locProjection, locTemp, locLightPos;
    private int locView2, locProjection2;
    private int locViewGrid, locProjectionGrid;
    private int locViewElephant, locProjectionElephant ,locLightPosElephant,shaderProgramElephantSun;
    private int locViewElephantSun, locProjectionElephantSun ,locLightPosElephantSun;

    private Camera camera;
    private Camera cameraLight;
    private Mat4 projection;

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
        shaderProgramGrid = ShaderUtils.loadProgram("/grid");
        shaderProgramElephant = ShaderUtils.loadProgram("/elephant");
        shaderProgramElephantSun = ShaderUtils.loadProgram("/elephantSun");

        locLightPos = glGetUniformLocation(shaderProgram1, "lightPos");
        locView = glGetUniformLocation(shaderProgram1, "view");
        locProjection = glGetUniformLocation(shaderProgram1, "projection");
        locTemp = glGetUniformLocation(shaderProgram1, "temp");

        locView2 = glGetUniformLocation(shaderProgram2, "view");
        locProjection2 = glGetUniformLocation(shaderProgram2, "projection");
        locViewGrid = glGetUniformLocation(shaderProgramGrid, "view");
        locProjectionGrid = glGetUniformLocation(shaderProgramGrid, "projection");

        locViewElephant = glGetUniformLocation(shaderProgramElephant, "view");
        locProjectionElephant = glGetUniformLocation(shaderProgramElephant, "projection");
        locLightPosElephant = glGetUniformLocation(shaderProgramElephant, "lightPos");

        locViewElephantSun = glGetUniformLocation(shaderProgramElephantSun, "view");
        locProjectionElephantSun = glGetUniformLocation(shaderProgramElephantSun, "projection");
        locLightPosElephantSun = glGetUniformLocation(shaderProgramElephantSun, "lightPos");


        modelElephant = new OGLModelOBJ("/obj/ElephantBody.obj");
        buffersElephant = modelElephant.getBuffers();

//*
        buffers = GridFactory.generateGridStrip(100, 100);
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
                .withPosition(new Vec3D(6, 6, 5)) // pozice pozorovatele
                .withAzimuth(5 / 4f * Math.PI) // otočení do strany o (180+45) stupňů v radiánech
                .withZenith(-1 / 5f * Math.PI); // otočení (90/5) stupňů dolů

        cameraLight = new Camera().withPosition(new Vec3D(5, 5, 0));

//        view = new Mat4ViewRH(
//                new Vec3D(4, 4, 4),
//                new Vec3D(-1, -1, -1),
//                new Vec3D(0, 0, 1)
//        );

        projection = new Mat4PerspRH(Math.PI / 3, 600 / 800f, 1, 20);
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
        //render1();
        //render2();

        //gridRender();
        renderElephant();
        gridRenderElephant();
        //viewer.view(texture1, -1, -1, 0.5);
        //viewer.view(renderTarget.getColorTexture(), -1, -0.5, 0.5);
        cameraLight = cameraLight.withPosition(new Vec3D(
                cameraLight.getPosition().getX()*Math.cos(otoceni)-cameraLight.getPosition().getY()*Math.sin(otoceni),
                cameraLight.getPosition().getX()*Math.sin(otoceni)+cameraLight.getPosition().getY()*Math.cos(otoceni),
                cameraLight.getPosition().getZ()
        ));
        otoceni=otoceni+0.00001;
        System.out.println(cameraLight.getPosition());
        textRenderer.addStr2D(LwjglWindow.WIDTH - 500, LwjglWindow.HEIGHT - 3, camera.getPosition().toString() + " azimut: "+ camera.getAzimuth() + " zenit: "+ camera.getZenith());
    }

    private void render1() {
        glUseProgram(shaderProgram1);

        renderTarget.bind();
        glClearColor(0.4f, 0, 0, 1);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glUniform3fv(locLightPos, ToFloatArray.convert(cameraLight.getPosition()));
        glUniformMatrix4fv(locView, false, camera.getViewMatrix().floatArray());
        glUniformMatrix4fv(locProjection, false, projection.floatArray());

        texture1.bind(shaderProgram1, "texture1", 0);

        glUniform1f(locTemp, 1.0f);
        buffers.draw(GL_TRIANGLE_STRIP, shaderProgram1);
        //buffers.draw(GL_TRIANGLE_STRIP,shaderProgram1);
    }

    private void render2() {
        glUseProgram(shaderProgram2);

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glClearColor(0, 0.4f, 0, 1);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glViewport(0, 0, LwjglWindow.WIDTH, LwjglWindow.HEIGHT);

        glUniformMatrix4fv(locView2, false, camera.getViewMatrix().floatArray());
        glUniformMatrix4fv(locProjection2, false, projection.floatArray());

        texture1.bind(shaderProgram2, "texture1", 0);
        //renderTarget.getColorTexture().bind(shaderProgram2, "texture1", 0);

        buffers.draw(GL_TRIANGLES, shaderProgram2);
    }
    private void gridRender(){
        glUseProgram(shaderProgramGrid);

        //renderTarget.bind();

        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        glClearColor(0, .1f, 0, 1);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glViewport(0, 0, LwjglWindow.WIDTH, LwjglWindow.HEIGHT);


        //glUniform3fv(locLightPos, ToFloatArray.convert(cameraLight.getPosition()));
        glUniformMatrix4fv(locViewGrid, false, camera.getViewMatrix().floatArray());
        glUniformMatrix4fv(locProjectionGrid, false, projection.floatArray());

        texture1.bind(shaderProgramGrid, "texture1", 0);
        buffers.draw(GL_TRIANGLE_STRIP, shaderProgramGrid);
    }

    private void gridRender2(){
        glUseProgram(shaderProgramGrid);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        glViewport(0, 0, 800, 600);


        //glUniform3fv(locLightPos, ToFloatArray.convert(cameraLight.getPosition()));
        glUniformMatrix4fv(locView2, false, camera.getViewMatrix().floatArray());
        glUniformMatrix4fv(locProjection2, false, projection.floatArray());

        //texture1.bind(shaderProgramGrid, "texture1", 0);
        buffers.draw(GL_TRIANGLES, shaderProgramGrid);
    }
    private void renderElephant(){
        glUseProgram(shaderProgramElephant);

        //renderTarget.bind();
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);

        glClearColor(0, .1f, 0, 1);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        glViewport(0, 0, LwjglWindow.WIDTH, LwjglWindow.HEIGHT);


        glUniform3fv(locLightPosElephant, ToFloatArray.convert(cameraLight.getPosition()));
        glUniformMatrix4fv(locViewElephant, false, camera.getViewMatrix().floatArray());
        glUniformMatrix4fv(locProjectionElephant, false, projection.floatArray());

        //texture1.bind(shaderProgramGrid, "texture1", 0);
        buffersElephant.draw(modelElephant.getTopology(), shaderProgramElephant);
    }
    private void gridRenderElephant(){
        glUseProgram(shaderProgramElephantSun);

        //renderTarget.bind();

        //glBindFramebuffer(GL_FRAMEBUFFER, 0);
//
        //glClearColor(0, .1f, 0, 1);
        //glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
//
        //glViewport(0, 0, LwjglWindow.WIDTH, LwjglWindow.HEIGHT);


        glUniform3fv(locLightPosElephantSun, ToFloatArray.convert(cameraLight.getPosition()));
        glUniformMatrix4fv(locViewElephantSun, false, camera.getViewMatrix().floatArray());
        glUniformMatrix4fv(locProjectionElephantSun, false, projection.floatArray());

        //texture1.bind(shaderProgramGrid, "texture1", 0);
        buffers.draw(GL_TRIANGLE_STRIP, shaderProgramElephantSun);
    }

//    @Override
//    public GLFWWindowSizeCallback getWsCallback() {
//        return null; // FIXME
//    }

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
                }
            }
        }
    };

}
