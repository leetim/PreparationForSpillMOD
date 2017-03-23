/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package preparationforspillmod;

//1146 -- размерность полей

import ucar.ma2.*;
import ucar.nc2.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author artem
 */
public class PreparationForSpillMOD {

    /**
     * @param args the command line arguments
   * @throws java.io.FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
      Date c = new Date();
      String[] s = new String[6];
      String[] s_wind = new String[1];
//      for (int i = 0; i < args.length; i++){
//        System.out.println(args[i]);
//      }
//      if (args.length == 0){
//        System.err.println("No args");
//        return;
//      }
//      SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMDD.'bin'");
//      System.out.println(sdf.format(new Date()));
      
      for (int i = 0; i < 6; i++){
        s[i] = "roms_his_0301.nc";
      }
      s_wind[0] = "roms_frc_20161029_00.nc";
      PreparatorWave p_wave = new PreparatorWave(s, c);
      PreparatorWind p_wind = new PreparatorWind(s_wind, c);
      try {
        p_wave.read();
      } catch (Exception ex) {
        ex.printStackTrace();
      }
      p_wave.close();
      try {
        p_wind.read();
      } catch (Exception ex) {
        ex.printStackTrace();
      }
      p_wind.close();
//      System.out.println(p_wave.get_hour());
    }
    
}
