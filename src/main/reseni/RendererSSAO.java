package main.reseni;

import lwjglutils.*;
import main.*;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;
import transforms.*;

import java.io.IOException;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class RendererSSAO extends AbstractRenderer {

    private OGLBuffers buffersDuck,buffersElephant ,buffersSun,quad,buffers,buffersStrip;
    private double otoceni =0.01;

    private int locViewSun, locProjectionSun, shaderProgramSun, locLightPosSun,locLightDirSun, locLightSpotCutOffSun,locLightTypeSun;
    private int shaderProgramSSAO,shadeProgram,shaderProgramKoule,locShadeLightType,locShadeLightDir,locShadeLightSpotCutOff;
    private int locTempKoule , locViewKoule,locProjectionKoule,locDeformKoule ;
    private int locSsaoProjection,locSsaoView;
    private int locShadeView,locShadeLightPos,locShadeCameraPos,locShadeSvetloADS;
    private Camera camera;
    private Camera cameraLight;
    private Mat4 projection;
    private boolean per,debug;
    OGLModelOBJ modelElephant,modelDuck;
    private OGLTexture2D texture1,randomTexture;
    private OGLTexture2D.Viewer viewer;
    private OGLRenderTarget prvniRT, druhyRT, tretiRT;
    private Vec3D lightDir = new Vec3D(1,0,0);
    private float lightSpotCutOff = 0.95f;
    private float deformVar = 0f;
    private int switchInt =0;
    private float lightType = 0;
    private int svetloAmbient=0,svetloDiffuse=0,svetloSpecular=0;

    @Override
    public void init() {
        OGLUtils.printOGLparameters();
        OGLUtils.printLWJLparameters();
        OGLUtils.printJAVAparameters();
        OGLUtils.shaderCheck();

        textRenderer = new OGLTextRenderer(LwjglWindow.WIDTH, LwjglWindow.HEIGHT);

        shaderProgramKoule = ShaderUtils.loadProgram("/SSAO/koule");
        shaderProgramSSAO = ShaderUtils.loadProgram("/SSAO/ssao");
        shaderProgramSun = ShaderUtils.loadProgram("/SSAO/objSun");
        shadeProgram = ShaderUtils.loadProgram("/SSAO/shade");

        locTempKoule = glGetUniformLocation(shaderProgramKoule, "temp");
        locViewKoule = glGetUniformLocation(shaderProgramKoule, "view");
        locProjectionKoule = glGetUniformLocation(shaderProgramKoule, "projection");
        locDeformKoule = glGetUniformLocation(shaderProgramKoule, "deformVar");

        locSsaoProjection = glGetUniformLocation(shaderProgramSSAO, "projection");
        locSsaoView = glGetUniformLocation(shaderProgramSSAO, "view");

        locShadeView = glGetUniformLocation(shadeProgram, "view");
        locShadeLightPos = glGetUniformLocation(shadeProgram, "lightPosition");
        locShadeCameraPos = glGetUniformLocation(shadeProgram, "cameraPosition");
        locShadeLightType = glGetUniformLocation(shadeProgram, "lightType");
        locShadeLightSpotCutOff = glGetUniformLocation(shadeProgram, "lightSpotCutOff");
        locShadeLightDir = glGetUniformLocation(shadeProgram, "lightDir");
        locShadeSvetloADS = glGetUniformLocation(shadeProgram, "svetloADS");

        //locViewDuck = glGetUniformLocation(shaderProgramDuck, "view");
        //locProjectionDuck = glGetUniformLocation(shaderProgramDuck, "projection");
        //locLightPosDuck = glGetUniformLocation(shaderProgramDuck, "lightPos");
        //locCameraPosDuck = glGetUniformLocation(shaderProgramDuck, "cameraPos");

        locViewSun = glGetUniformLocation(shaderProgramSun, "view");
        locProjectionSun = glGetUniformLocation(shaderProgramSun, "projection");
        locLightPosSun = glGetUniformLocation(shaderProgramSun, "lightPos");
        locLightDirSun = glGetUniformLocation(shaderProgramSun, "lightDir");
        locLightSpotCutOffSun = glGetUniformLocation(shaderProgramSun, "lightSpotCutOff");
        locLightTypeSun = glGetUniformLocation(shaderProgramSun, "lightType");


        modelElephant = new OGLModelOBJ("/obj/ElephantBody.obj");
        buffersElephant = modelElephant.getBuffers();

        modelDuck= new OGLModelOBJ("/obj/ducky.obj");
        buffersDuck = modelDuck.getBuffers();
        quad = QuadFactory.getQuad();
//*
        buffers = GridFactory.generateGrid(100,100);
        buffersStrip = GridFactory.generateGridStrip(100,100);
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

        cameraLight = new Camera().withPosition(new Vec3D(-2, -2, 2));

//        view = new Mat4ViewRH(
//                new Vec3D(4, 4, 4),
//                new Vec3D(-1, -1, -1),
//                new Vec3D(0, 0, 1)
//        );

        projection = new Mat4PerspRH(Math.PI / 3, LwjglWindow.HEIGHT / (float)LwjglWindow.WIDTH, 1, 20);
//        projection = new Mat4OrthoRH(10, 7, 1, 20);

        try {
            texture1 = new OGLTexture2D("./textures/mramor2.jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }

        viewer = new OGLTexture2D.Viewer();

        prvniRT = new OGLRenderTarget(2560, 1440,4);
        druhyRT = new OGLRenderTarget(2560, 1440);
        randomTexture = RandomTextureGenerator.getTexture();
    }

    @Override
    public void display() {
        glEnable(GL_DEPTH_TEST);
        perspective();
        clearAndViewPort();

        renderObjekt();
        renderSSAO();
        renderFinal();
        renderSunPos();
        cameraLight = cameraLight.withPosition(new Vec3D(
                cameraLight.getPosition().getX()*Math.cos(otoceni)-cameraLight.getPosition().getY()*Math.sin(otoceni),
                cameraLight.getPosition().getX()*Math.sin(otoceni)+cameraLight.getPosition().getY()*Math.cos(otoceni),
                cameraLight.getPosition().getZ()
        ));
        //System.out.println(lightSpotCutOff);
        lightDir = new Vec3D(cameraLight.getPosition().mul(-1));
        if(!debug){
            viewer.view(prvniRT.getColorTexture(0),-1,0,0.5);
            viewer.view(prvniRT.getColorTexture(1),-1, -0.5, 0.5);
            viewer.view(prvniRT.getColorTexture(2),-0.5, -0.5, 0.5);
            viewer.view(prvniRT.getDepthTexture(), -0.5, -1, 0.5);
            viewer.view(druhyRT.getColorTexture(), -1, -1, 0.5);
            //viewer.view(randomTexture,-0.5, 0, 0.5);
            textRenderer.addStr2D(LwjglWindow.WIDTH - 500, LwjglWindow.HEIGHT - 3, camera.getPosition().toString() + " azimut: "+ camera.getAzimuth() + " zenit: "+ camera.getZenith());
            textRenderer.addStr2D(LwjglWindow.WIDTH - 500, LwjglWindow.HEIGHT - 15, "refrektor " + lightSpotCutOff);
        }
    }

    private void renderObjekt(){
        glUseProgram(shaderProgramKoule);

        prvniRT.bind();
        glClearColor(0.0f, 0.1f, 0.0f, 1);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        deformVar=(deformVar+0.025f)%((float) Math.PI*4);
        glUniformMatrix4fv(locViewKoule, false, camera.getViewMatrix().floatArray());
        glUniformMatrix4fv(locProjectionKoule, false, projection.floatArray());
        glUniform1f(locDeformKoule, deformVar);

        texture1.bind(shaderProgramKoule, "texture1", 0);
        glUniform1f(locTempKoule, 0.0f);
        buffers.draw(GL_TRIANGLES, shaderProgramKoule);
        glUniform1f(locTempKoule, 1.0f);
        buffers.draw(GL_TRIANGLES, shaderProgramKoule);
        glUniform1f(locTempKoule, 2.0f);
        buffers.draw(GL_TRIANGLES, shaderProgramKoule);
        glUniform1f(locTempKoule, 3.0f);
        buffers.draw(GL_TRIANGLES, shaderProgramKoule);
        glUniform1f(locTempKoule, 4.0f);
        buffers.draw(GL_TRIANGLES, shaderProgramKoule);
        glUniform1f(locTempKoule, 5.0f);
        buffers.draw(GL_TRIANGLES, shaderProgramKoule);
        glUniform1f(locTempKoule, 6.0f);
        switch (switchInt){
            case 0:
                buffers.draw(GL_TRIANGLES, shaderProgramKoule);
                break;
            case 1:
                buffers.draw(GL_LINES, shaderProgramKoule);
                break;
            case 2:
                buffers.draw(GL_POINTS, shaderProgramKoule);
                break;
            case 3:
                buffersStrip.draw(GL_TRIANGLE_STRIP, shaderProgramKoule);
                break;
            case 4:
                buffersStrip.draw(GL_LINES, shaderProgramKoule);
                break;
            case 5:
                buffersStrip.draw(GL_POINTS, shaderProgramKoule);
                break;
        }
        glUniform1f(locTempKoule, 7.0f);
        buffersElephant.draw(modelElephant.getTopology(), shaderProgramKoule);
        glUniform1f(locTempKoule, 8.0f);
        buffersDuck.draw(modelDuck.getTopology(), shaderProgramKoule);
    }

    private void renderSSAO(){
        glUseProgram(shaderProgramSSAO);
        druhyRT.bind();
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glDisable(GL_DEPTH_TEST);

        prvniRT.bindColorTexture(shaderProgramSSAO, "positionTexture", 0, 3);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

        randomTexture.bind(shaderProgramSSAO, "randomTexture", 1);

        glUniformMatrix4fv(locSsaoView, false, camera.getViewMatrix().floatArray());
        glUniformMatrix4fv(locSsaoProjection, false, projection.floatArray());
        quad.draw(GL_TRIANGLES, shaderProgramSSAO);
    }

    private void renderFinal() {
        glUseProgram(shadeProgram);
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
        glViewport(0, 0, LwjglWindow.WIDTH, LwjglWindow.HEIGHT);
        glClearColor(0.0f, 0.0f, 0.1f, 1);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

        prvniRT.bindColorTexture(shadeProgram, "positionTexture", 0, 0);
        druhyRT.bindColorTexture(shadeProgram, "ssaoTexture", 1, 0);
        prvniRT.bindColorTexture(shadeProgram, "imageTexture", 2,1);
        prvniRT.bindColorTexture(shadeProgram, "imageColor", 3,2);

        glUniform3fv(locShadeLightDir, ToFloatArray.convert(lightDir));
        glUniform1f(locShadeLightSpotCutOff, lightSpotCutOff);
        glUniform1f(locShadeLightType, lightType);
        glUniform3fv(locShadeCameraPos, ToFloatArray.convert(camera.getPosition()));
        glUniform3fv(locShadeLightPos, ToFloatArray.convert(cameraLight.getPosition()));
        glUniformMatrix4fv(locShadeView, false, camera.getViewMatrix().floatArray());
        Point3D svetlo = new Point3D(svetloAmbient,svetloDiffuse,svetloSpecular);
        glUniform3fv(locShadeSvetloADS,ToFloatArray.convert(svetlo));

        quad.draw(GL_TRIANGLES, shadeProgram);
    }

    private void renderSunPos(){
        glUseProgram(shaderProgramSun);

        glUniform3fv(locLightPosSun, ToFloatArray.convert(cameraLight.getPosition()));
        glUniform3fv(locLightDirSun, ToFloatArray.convert(lightDir));
        glUniform1f(locLightSpotCutOffSun, lightSpotCutOff);
        glUniform1f(locLightTypeSun, lightType);
        glUniformMatrix4fv(locViewSun, false, camera.getViewMatrix().floatArray());
        glUniformMatrix4fv(locProjectionSun, false, projection.floatArray());

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
                    -20*LwjglWindow.HEIGHT / (float)LwjglWindow.WIDTH,
                    20*LwjglWindow.HEIGHT / (float)LwjglWindow.WIDTH,
                    1,20);
        }
    }

    @Override
    public GLFWWindowSizeCallback getWsCallback() {
        return windowResCallback;
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
                    case GLFW_KEY_M :
                        switchInt = (switchInt+1)%6;
                        break;
                    case GLFW_KEY_L :
                        lightType = (lightType+1)%2;
                        break;
                    case GLFW_KEY_B :
                        lightSpotCutOff =((lightSpotCutOff+ 0.002f)+1)%1;
                        break;
                    case GLFW_KEY_V :
                        lightSpotCutOff =((lightSpotCutOff- 0.002f)+1)%1;
                        break;
                    case GLFW_KEY_C :
                        svetloSpecular=(svetloSpecular+1)%2;
                        break;
                    case GLFW_KEY_X:
                        svetloDiffuse=(svetloDiffuse+1)%2;
                        break;
                    case GLFW_KEY_Z :
                        svetloAmbient=(svetloAmbient+1)%2;
                        break;
                    case GLFW_KEY_G :
                        debug=!debug;
                        break;
                }
            }
        }
    };
    public static void main(String[] args) {
        new LwjglWindow(new RendererSSAO());
    }

}
