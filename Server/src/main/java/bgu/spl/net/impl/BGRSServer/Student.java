package bgu.spl.net.impl.BGRSServer;

import java.util.LinkedList;
import java.util.concurrent.locks.ReentrantLock;

public class Student {

    private final String username;
    private final String password;
    private LinkedList<Course> courses;
    private final ReentrantLock lock = new ReentrantLock(true);
    private final boolean admin;

    private boolean login;

    public Student(String _username, String _password, boolean _admin){
        username = _username;
        password = _password;
        courses = new LinkedList<Course>();
        login = false;
        admin = _admin;
    }

    public boolean isAdmin(){ return admin; }

    public boolean correctPass(String _pass){
        return _pass.equals(password);
    }

    public LinkedList<Course> getCourses() {
        return courses;
    }

    public String getStringCourses(){
        Course.sortCourses(courses);
        String ans = "[";
        for(Course c:courses)
            ans+=c.getSerial()+",";
        if(courses.size()>0)
            ans = ans.substring(0,ans.length()-1);
        ans+="]";
        return ans;
    }

    public boolean register(Course course){
        LinkedList<Course> kdam = course.getKdamCourses();
        if(courses.contains(course) || course.isFull() || course.hasStudent(username) )
            return false;
        for(Course c:kdam)
            if(!courses.contains(c)){
                return false;
            }

        courses.add(course);
        return true;
    }

    public boolean unregister(Course course){
        if(courses.contains(course)) {
            courses.remove(course);
            return true;
        }
        return false;
    }

    /** Maybe server will handle all below this **/
    public boolean login(String pass){
        if(!login && pass.equals(password)) {
            login = true;
            lock.unlock();
            return true;
        }
        else
            return false;

    }

    public ReentrantLock getLock(){
        return lock;
    }
    public  boolean logout(){
        if(!login){
            return false;
        }
        login = false;
        return true;
    }

    public boolean isLogin(){
        return login;
    }


}

