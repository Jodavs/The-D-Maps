package edu.itu.the_d.map;

import edu.itu.the_d.map.controller.MapViewController;
import edu.itu.the_d.map.controller.ViewController;
import edu.itu.the_d.map.model.Model;
import edu.itu.the_d.map.model.mapobjects.ColorTheme;
import edu.itu.the_d.map.utils.*;
import edu.itu.the_d.map.view.MapView;
import edu.itu.the_d.map.view.View;

import java.awt.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Main class
 * <p>
 * Copyright 2016 The-D
 */
public class Main {
    public static void main(String[] args) {

        // For jar file
        if (args.length == 0) {
            try {
                Runtime.getRuntime().exec("java -jar -Xms8g The-D.jar default");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.exit(0);
        }

        ColorTheme.setDefaultTheme();
        long t1 = System.currentTimeMillis();
        try {
            if (args[0].equals("default")) new Main().run(null);
            else new Main().run(args[0]);
        } catch (Exception e) {
            LoadingView.turnOff();
            e.printStackTrace();
        }
        Debugger.print(User.GLOBAL, System.currentTimeMillis() - t1 + " ms to load.");
    }

    private void run(String filename) {
		/**
         * The following try-catch is taken from <a href="https://gist.githubusercontent.com/bchapuis/1562406/raw/e0c485675ef6e14c233ba9177005812a171b0431/dockicon.java">Github.com</a>
         * and is a lame but nessecary hack make the application able to compile on non-windows machines
         * while being able to show the icon in the doc on OSX machines.
         */
        try {
            Class util = Class.forName("com.apple.eawt.Application");
            Method getApplication = util.getMethod("getApplication", new Class[0]);
            Object application = getApplication.invoke(util);
            Class params[] = new Class[1];
            params[0] = Image.class;
            Method setDockIconImage = util.getMethod("setDockIconImage", params);
            setDockIconImage.invoke(application, ImageLoader.loadImage("theme1.png"));
        } catch (ClassNotFoundException e) {
            // log exception
        } catch (NoSuchMethodException e) {
            // log exception
        } catch (InvocationTargetException e) {
            // log exception
        } catch (IllegalAccessException e) {
            // log exception
        }

        Model m = Model.createModel(filename);
        MapView mv = new MapView(m);
        View v = new View(m, mv);
        mv.setView(v);
        new ViewController(m, v);
        new MapViewController(m, mv);
    }

    /**
     * Used to open a new file while the program is running.
     * @param filename of type String.
     */
    public void recreateWithNewFile(String filename) {
        Model m = Model.createModel(filename);
        MapView mv = new MapView(m);
        View v = new View(m, mv);
        mv.setView(v);
        new ViewController(m, v);
        new MapViewController(m, mv);
    }

}