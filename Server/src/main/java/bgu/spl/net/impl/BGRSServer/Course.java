package bgu.spl.net.impl.BGRSServer;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;
public class Course {


    private int serial;
    private final int sortNum;
    private String name;
    private LinkedList<Course> kdamCourses; //We sort this in constructor
    private LinkedList<Integer> kdamNum; //THIS HAS TO BE SORTED!!!!!
    private int maxStudents;
    private int numRegistered;
    private boolean full;
    private LinkedList<String> studentList;// We have to sort this
    private final ReentrantLock lock = new ReentrantLock(true);

    public String seatStat(){
        return (maxStudents-numRegistered)+"/"+maxStudents;
    }

    public ReentrantLock getLock(){
        return lock;
    }

    public Course(int _serial, int _sortNum, String _name, LinkedList<Course> _kdamCourses,
                  LinkedList<Integer> _kdamNum, int _maxStudents){
        serial = _serial;
        sortNum = _sortNum;
        name = _name;
        kdamCourses = _kdamCourses;
        kdamNum = _kdamNum;
//        kdamCourses.sort(Comparator.comparing(Course::getSortNum));
//        for(int i = 0; i<kdamCourses.size(); i++){
//            kdamNum.add(i,kdamCourses.get(i).getSerial());
//        }
        maxStudents = _maxStudents;
        numRegistered = 0;
        studentList = new LinkedList<>();
        full=false;
    }

    public LinkedList<Course> getKdamCourses() {
        return kdamCourses;
    }

    public LinkedList<Integer> getKdamNum() {
        return kdamNum;
    }

    public void addKdam(Course course){
        kdamCourses.add(course);
    }

    public int getSerial() {
        return serial;
    }

    public String getKdamString(){
        kdamCourses = sortCourses(kdamCourses);
        String ans="[";
        for(Course c: kdamCourses)
            ans+=c.getSerial()+",";
        if(kdamCourses.size()>0)
            ans = ans.substring(0,ans.length()-1);
        ans+="]";
        return ans;
    }

    public static LinkedList<Course> sortCourses(LinkedList<Course> courses){
        courses.sort(Comparator.comparing(Course::getSortNum));
        return courses;
    }

//    public static LinkedList<Integer> sortCoursesNum(LinkedList<Course> courses){
//        courses = sortCourses(courses);
//        LinkedList<Integer> coursesNum = new LinkedList<Integer>();
//        for(int i = 0; i<courses.size(); i++){
//            coursesNum.add(i,courses.get(i).getSerial());
//        }
//        return coursesNum;
//    }

    public int getSortNum() {
        return sortNum;
    }

    public String getName() {
        return name;
    }

    public String getStudents() {
        String s = "[";
        Collections.sort(studentList);
        for(String var : studentList){
            s = s + var + ",";
        }
        if(studentList.size()>0) {
            s = s.substring(0, s.length() - 1); //Removes the last ", "
        }
        s = s + "]";
        return s;
    }

    public boolean register(String studentName){
        //Maybe server checks if student is already registered and if course has max amount
        studentList.add(studentName);
        numRegistered++;
        if(numRegistered==maxStudents)
            full = true;
        return true;
    }

    public void unregister(String studentName){
            studentList.remove(studentName);
            numRegistered--;
            if(full)
                full=false;
    }

    public boolean isRegistered(String name){
        return studentList.contains(name);
    }

    public boolean isFull(){
        return !(maxStudents-numRegistered > 0);
    }

    public boolean hasStudent (String name){
        return studentList.contains(name);
    }


}


