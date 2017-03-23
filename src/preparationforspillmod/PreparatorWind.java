/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package preparationforspillmod;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import ucar.ma2.ArrayDouble;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 *
 * @author artem
 */
public class PreparatorWind extends PreparatorWave{

  /**
   *
   * @param fs
   * @param d
   * @throws FileNotFoundException
   */
  public PreparatorWind(String[] fs, Date d) throws FileNotFoundException {
    super(fs, d);
    out_format = new SimpleDateFormat("'wi'_yyyyMMdd.'bin'");
  }

  @Override
  public void read() throws FileNotFoundException, Exception{
    prep_out_file();
    int count = 1;
    String file_name = netCDF_names[0];
    try(NetcdfFile ncdf = NetcdfFile.open("./" + file_name)){
      Variable u = ncdf.findVariable("sustr");
      Variable v = ncdf.findVariable("svstr");
      ArrayDouble.D3 u_data = (ArrayDouble.D3)u.read(":, :, :").reduce();
      ArrayDouble.D3 v_data = (ArrayDouble.D3)v.read(":, :, :").reduce();
      u_data = get_interpoled(u_data, wanted_shape);
      v_data = get_interpoled(v_data, wanted_shape);
      for (int i = 0; i < 5*8; i++){
        int max = 3;
        if (i == 5*8-1){
          max++;
        }
        for (int j = 0; j < max; j++){
          bw.writeInt(count);
          count++;
          write_f(get_D2(u_data, i));
          write_f(get_D2(v_data, i));
        }
      }
    }
    catch (IOException | InvalidRangeException ex){
      Logger.getLogger(PreparatorWave.class.getName()).log(Level.SEVERE, null, ex);
    }
    System.out.println(count);

  }

  @Override
  public short prep_value(double val){
    return (short)(val*100);
  }

  // public ArrayDouble.D2 get_D2(ArrayDouble.D4 a, int t1, int t2){
  //   int[] shape = a.getShape();
  //   ArrayDouble.D2 temp = new ArrayDouble.D2(shape[2], shape[3]);
  //   for (int j = 0; j < shape[3]; j++){
  //     for (int i = 0; i < shape[2]; i++){
  //       temp.set(i, j, a.get(t1, t2, i, j));
  //     }
  //   }
  //   return temp;
  // }

}
