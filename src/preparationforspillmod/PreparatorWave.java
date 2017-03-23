package preparationforspillmod;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import LRFD.DataStructures.Data3DField;
import ucar.ma2.*;
import ucar.nc2.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import LRFD.Methods.Interpolation;


/**
 *
 * @author artem
 */
public class PreparatorWave {
  protected String[] netCDF_names;
  protected Date date;
  protected DataOutputStream bw;
  protected SimpleDateFormat out_format;
  protected int wanted_shape[] = {1146, 1146};
  PreparatorWave(String[] fs, Date d) throws FileNotFoundException{
    netCDF_names = fs;
    out_format = new SimpleDateFormat("'uv'_yyyyMMdd'.bin'");
    date = d;
  }
  
  public void prep_out_file() throws FileNotFoundException{
    OutputStream fos = new FileOutputStream(out_format.format(date));
    BufferedOutputStream bos = new BufferedOutputStream(fos);
    bw = new DataOutputStream(bos);
  }

  public void close() throws IOException{
    bw.close();
  }

  public short prep_value(double val){
    return (short) (val*1000);
  }
  //Calendar d1
  public int get_hour(){
    return date.getHours();
  }

  public void write_f(ArrayDouble.D2 arr) throws IOException{
//    System.out.println("adding");
    int[] shape = arr.getShape();
    for (int j = 0; j < shape[1]; j++){
      for (int i = 0; i < shape[0]; i++){
        short temp = prep_value(arr.get(i, j));
        if (arr.get(i, j) > 100){
          temp = 0;
        }
        bw.writeShort(Short.reverseBytes(temp));
      }
    }
  }

  public ArrayDouble.D2 get_D2(ArrayDouble.D3 a, int t){
    int[] shape = a.getShape();
    ArrayDouble.D2 temp = new ArrayDouble.D2(shape[1], shape[2]);
    for (int i = 0; i < shape[1]; i++){
      for (int j = 0; j < shape[2]; j++){
        temp.set(i, j, a.get(t, i, j));
      }
    }
    return temp;
  }
  
  public ArrayDouble.D3 get_interpoled(ArrayDouble.D3 a, int new_count[]) throws Exception{
    int[] shape = a.getShape();
    ArrayDouble.D3 res = new ArrayDouble.D3(shape[0], new_count[0], new_count[1]);
    Data3DField df_cur = new Data3DField();
    Data3DField df_wanted = new Data3DField();
    
    df_cur.time = new double[shape[0]];
    df_cur.lat = new double[shape[1]];
    df_cur.lon = new double[shape[2]];
    df_cur.data = new double[shape[0]][shape[1]][shape[2]];
    df_wanted.time = new double[shape[0]];
    df_wanted.lat = new double[new_count[0]];
    df_wanted.lon = new double[new_count[1]];
    df_wanted.data = new double[shape[0]][new_count[0]][new_count[1]];
    
    double h1 = (double)(shape[1] - 1)/(new_count[0] - 1);
    double h2 = (double)(shape[2] - 1)/(new_count[1] - 1);
    
    for (int i = 0; i < shape[0]; i++){
      df_cur.time[i] = i;
      df_wanted.time[i] = i;
    }
    for (int i = 0; i < shape[1]; i++){
      df_cur.lat[i] = i;      
    }
    for (int i = 0; i < shape[2]; i++){
      df_cur.lon[i] = i;      
    }
    for (int i = 0; i < new_count[0]; i++){
      df_wanted.lat[i] = i*h1;
    }
    for (int i = 0; i < new_count[1]; i++){
      df_wanted.lon[i] = i*h2;
    }
    for (int t = 0; t < shape[0]; t++){
      for (int i = 0; i < shape[1]; i++){
        for (int j = 0; j < shape[2]; j++){
          df_cur.data[t][i][j] = a.get(t, i, j);
        }
      }
    }
    df_wanted = Interpolation.BilinearInterpolation(df_cur, df_wanted);
    for (int t = 0; t < shape[0]; t++){
      for (int i = 0; i < shape[1]; i++){
        for (int j = 0; j < shape[2]; j++){
          res.set(t, i, j, df_wanted.data[t][i][j]);
        }
      }
    }
    return res;
  }

  public void read() throws FileNotFoundException, Exception {
    prep_out_file();
//    System.err.println("No args");
    int count = 1;
    for (int i = 0; i < netCDF_names.length; i++){
      String file_name = netCDF_names[i];
      try{
        NetcdfFile ncdf = NetcdfFile.open("./" + file_name);
        int min = 0;
        int max = 24;
        if (i == netCDF_names.length-1){
          max = 1;
        }
        Variable u = ncdf.findVariable("u");
//        System.err.println("No args");
        Variable v = ncdf.findVariable("v");
        ArrayDouble.D3 u_data = (ArrayDouble.D3)u.read(":, 31, :, :").reduce();
        ArrayDouble.D3 v_data = (ArrayDouble.D3)v.read(":, 31, :, :").reduce();
        u_data = get_interpoled(u_data, wanted_shape);
        v_data = get_interpoled(v_data, wanted_shape);
        for (int j = min; j < max; j++){
          bw.writeInt(count);
          count++;
          write_f(get_D2(u_data, j));
          write_f(get_D2(v_data, j));
        }
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
    }
    System.out.println(count);

  }

}
