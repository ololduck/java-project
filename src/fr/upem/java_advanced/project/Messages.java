package fr.upem.java_advanced.project;

/**
 * Created with my hands.
 * User: dr
 * Date: 11/28/13
 * Time: 5:43 PM.
 */
public class Messages {

  public static  String getOutputString(String option,String param){
        switch(option){
            case "e":
            case "E":
            case "endWith":
                return "File name must not end with :"+param ;
            case "i":
            case "I":
            case "interdit":
            case "forceinterdit":
            case "forbidden":
            case "forceforbidden":
                return "Forbidden file name:"+param ;
            case "x":
            case "X":
            case "existe":
            case "forceexiste":
                return "Missing file name :"+param ;
            case "b":
            case "B":
            case "forcebeginsWith":
            case "beginsWith":
                return "File name must not start with :"+param ;
            case "o":
            case "O":
            case "onetop":
            case "forceonetop":
                return "Archive with more than one top directory";
            default:
                return "UnKnown option" ;
        }
    }

}
