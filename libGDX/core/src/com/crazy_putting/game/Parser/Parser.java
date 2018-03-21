package com.crazy_putting.game.Parser;

import com.badlogic.gdx.math.Vector2;
import com.crazy_putting.game.GameObjects.Course;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class Parser {

    private static String _courseSeparator = "\nCOURSE:";
    private static String _fileNameCached = "";
    private static List<Course> _cacheFile = null;
    public static void readCourse(String pFileName) throws  IOException
    {

        List<String> lines = Files.readAllLines(Paths.get(pFileName));
       _cacheFile = generateCourses(lines);
       _fileNameCached = pFileName;
    }
    public static void writeCourse(String pFileName, Object pCourse)
    {
    if( checkForCache() == false) cacheFile(pFileName);
       List<String> out = new ArrayList<String>();//= getCacheFile();
        out.add(_courseSeparator + "");
        out.add( "\nID: " +  (getAmountCourses() + 1));//Set the next course ID
        out.add("\nName: ");//+pCourse.getName();
        out.add("\nHeight: " );//+ pCourse.getHeight();
        out.add("\nFriction: "); // +pCourse.getFriction();
        out.add("\nGoal Pos: "); //+ pCourse.getGoalPos();
        out.add("\nGoal Radius: "); //+ pCourse.getGoalRadius();
        out.add( "\nBall Start Pos: "); //+ pCourse.getBallStartPos();
        out.add("\nMax Speed: "); //+ pCourse.getMaxSpeed()
       String finalFile = "";
        for(int i = 0; i < out.size(); i++) {
            finalFile += out.get(i);

        }
        try{
            writeToTextFile(pFileName, finalFile);
        }catch(IOException e){
            System.out.println(e);
        }
    }
    private static void writeToTextFile(String fileName, String content) throws IOException {
        Files.write(Paths.get(fileName), content.getBytes(), StandardOpenOption.CREATE);
    }
    private static int getAmountCourses()
    {
        return 0;
    }
    private static void cacheFile(String pFileName)
    {
        try{
            readCourse(pFileName);
        }catch (IOException e){
            System.out.println(e);
        }
    }
    public static void updateCache(String pFileName)
    {
        cacheFile(pFileName);
    }
    private static List<Course> getCacheFile()
    {
        return _cacheFile;
    }
    private static  boolean checkForCache()
    {
        if(getCacheFile() == null)
         return false;
        else return  true;
    }
    private static List<Course> generateCourses(List<String> pLines)
    {

    List<Course> courses = new ArrayList<Course>();
    int lineCount = 0;
    boolean readingCourse = false;
    Course newCourse = null;
        for(int i = 0; i < pLines.size(); i++) {
            String line = pLines.get(i);
            if (lineCount > 7) {
                courses.add(newCourse);
                newCourse = null;
                readingCourse = false;
                lineCount = 0;
            }
            if (readingCourse) {
                setCourseProperty(newCourse, line, lineCount);
                lineCount++;
            }
            if (line.startsWith(_courseSeparator)) {
                readingCourse = true;
                newCourse = new Course();
            }
        }
        return courses;
    }
    private static void setCourseProperty(Course pCourse, String pProperty, int pLine)
        {
            switch (pLine)
            {
                case 0:
                    pProperty.replace("ID: ","");
                    pCourse.setID(Integer.parseInt(pProperty));
                    break;
                case 1:
                    pProperty.replace("Name: ","");
                    pCourse.setName(pProperty);
                    break;
                case 2:
                    pProperty.replace("Height: ","");
                    pCourse.setHeight(pProperty);
                    break;
                case 3:
                    pProperty.replace("Friction: ","");
                    pCourse.setFriction(Float.parseFloat(pProperty));
                    break;
                case 4:
                    pProperty.replace("Goal Pos: ","");
                    String[] GoalPos = pProperty.trim().split("\\s+");
                    pCourse.setGoalPosition(new Vector2(Float.parseFloat(GoalPos[0]), Float.parseFloat(GoalPos[1])));
                    break;
                case 5:
                    pProperty.replace("Goal Radius: ","");
                    pCourse.setGoalRadius(Float.parseFloat(pProperty));
                    break;
                case 6:
                    pProperty.replace("Ball Start Pos: ","");
                    String[] ballStartPos = pProperty.trim().split("\\s+");
                    pCourse.setBallStartPos(new Vector2(Float.parseFloat(ballStartPos[0]), Float.parseFloat(ballStartPos[1])));
                    break;
                case 7:
                    pProperty.replace("Max Speed: ","");
                    pCourse.setMaxSpeed(Float.parseFloat(pProperty));

                    break;

            }
        }
    public static List<Course> getCourses(String pFileName)
    {
        try {
            if (checkForCache() == false ||_fileNameCached.equals(pFileName) == false)
                readCourse(pFileName);
        }catch(IOException e){
            System.out.println(e);}
        return _cacheFile;

    }


}
