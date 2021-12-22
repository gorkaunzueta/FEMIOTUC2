package com.test.influxtest;

import VehData2Euler.MATLABEulerClass;
import Online_Novelty.NoveltyClass;

import com.mathworks.toolbox.javabuilder.MWException;
import com.mathworks.toolbox.javabuilder.MWNumericArray;

import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.LinkedList;

import static java.lang.Math.*;

public class InfluxtestApplication {

    public static void main(String[] args) throws ParseException{

        /* Definition of URL, USER and PASSWORD for InfluxDB connectivity */
        //final String serverURL = "http://influxdb:8086", username = "femiot", password = "mcia1234"; //FOR DOCKER
        final String serverURL = "http://localhost:8086", username = "femiot", password = "mcia1234"; //FOR IntelliJ

        /* Connect to InfluxDB */
        final InfluxDB influxDB = InfluxDBFactory.connect(serverURL,username,password);

        /* Select database desired */
        String databaseName = "UC2";
        influxDB.setDatabase(databaseName);

        /* Set default configuration */
        influxDB.enableBatch(BatchOptions.DEFAULTS);

        /* Define variable to keep track of the last timestamp processed */
        String last_acquired = "'1970-01-01T00:00:00.000Z'";

        /* Define a formatter to parse the Time String provided by InfluxDB into the time in milliseconds */
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

        /* Definition of variable to store the result of the queries */
        QueryResult queryResult;

        /* Linked lists to store the last 300 values of vehicle and processed data (over 5 minutes of data) */
        LinkedList<VehData> VehList = new LinkedList<>();
        LinkedList<ProcessedData> ProcessedList = new LinkedList<>();

        /* Variable to loop over permanently except in case of error */
        boolean var = true;

        /* Beginning of the main loop */
        while (var) {

            /* Obtain all values stored which have not been processed */
            queryResult = influxDB.query(new Query("SELECT * FROM vehicledataset WHERE time > "+last_acquired));

            /* This is here in case it is necessary to sort the columns of the database automatically */
            //List<String> columns =  ((queryResult.getResults().get(0)).getSeries().get(0)).getColumns();

            /* Check if data has been obtained from the query */
            if((queryResult.getResults().get(0)).getSeries() != null ){

                /* Store the values of the query on a List */
                List<List<Object>> values =  ((queryResult.getResults().get(0)).getSeries().get(0)).getValues();

                /* Iterate over all the results obtained from the query */
                for (List<Object> next : values){

                    /* Store the time of the current iteration as string*/
                    String temporal = (String) next.get(0);
                    temporal = temporal.substring(0,temporal.length()-1); // Delete las character
                    /* Add zeros at the end of the string in order to make the length constant */
                    if(temporal.length() == 19){
                        temporal = temporal.concat(".");
                    }
                    while(temporal.length() < 23) {
                        temporal = temporal.concat("0");
                    }

                    /* Definition of variables to store the data from the vehicle and the data obtained by the process algorithm */
                    VehData Current = new VehData();
                    var Output = new ProcessedData();

                    /* Store each parameter in its corresponding place in the current class */
                    Current.time = (formatter.parse(temporal)).getTime(); // Add 1 hour for timezone if used in intellij

                    Current.Acc = new ArrayList<>();
                    Current.Acc.add((Double) next.get(1));
                    Current.Acc.add((Double) next.get(2));
                    Current.Acc.add((Double) next.get(3));

                    Current.Mag = new ArrayList<>();
                    Current.Mag.add((Double) next.get(4));
                    Current.Mag.add((Double) next.get(5));
                    Current.Mag.add((Double) next.get(6));

                    Current.Lat = (Double) next.get(10);
                    Current.Lon = (Double) next.get(11);

                    Current.Temp = (Double) next.get(12);
                    Current.Bat  = (Double) next.get(7);
                    Current.Gas  = (Double) next.get(8);

                    Current.id = (String) next.get(9);

                    /* Update last acquired value */
                    last_acquired = "'";
                    last_acquired = last_acquired.concat((String) next.get(0));
                    last_acquired = last_acquired.concat("'");

                    /* Process current values */
                    Output = ProcessAlgorithm(Current, VehList, ProcessedList);

                    /* Write output on the database */
                    influxDB.write(Point.measurement("ProData")
                            .time(Current.time, TimeUnit.MILLISECONDS)
                            .tag("id", Current.id)
                            .addField("NullInd", Output.NullInd)
                            .addField("State", Output.State)
                            .addField("Acc_fren", Output.Acc_fren)
                            .addField("ON/OFF", Output.ON_OFF)
                            .addField("Ramp", Output.Ramp)
                            .addField("StealWarning", Output.StealWarning)
                            .addField("AccidentWarnPos", Output.AccidentWarningP)
                            .addField("AccidentWarnML", Output.AccidentWarningML)
                            .addField("TempWarn", Output.TempWarning)
                            .addField("EulerZ", Output.EulerZ)
                            .addField("EulerY", Output.EulerY)
                            .addField("EulerX", Output.EulerX)
                            .addField("dadtx", Output.dadtx)
                            .addField("dadty", Output.dadty)
                            .addField("dadtz", Output.dadtz)
                            .addField("r", Output.r)
                            .addField("v", Output.v)
                            .build());

                    /* Delete data from the linked lists to leave space for the new values */
                    while(VehList.size() >= 50){
                        VehList.getFirst().dispose();
                        VehList.remove();
                        ProcessedList.getFirst().dispose();
                        ProcessedList.remove();
                    }

                    /* Add current values to the lists */
                    VehList.add(Current);
                    ProcessedList.add(Output);

                    System.gc();

                }
            }

            try {
                Thread.sleep(5_000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
                var = false;
            }
        }

        influxDB.close();

    }

    static class VehData{

        public Long time;
        public String id;
        public ArrayList<Double> Acc;
        public ArrayList<Double> Mag;
        public Double Lat;
        public Double Lon;
        public Double Temp;
        public Double Bat;
        public Double Gas;

        public void dispose(){
            this.time = null;
            this.id = null;
            this.Acc = null;
            this.Mag = null;
            this.Lat = null;
            this.Lon = null;
            this.Temp = null;
            this.Bat = null;
            this.Gas = null;
        }

    }

    static class ProcessedData{

        public Boolean  NullInd;
        public String   State;
        /* Driving service variables */
        public Integer  Acc_fren;
        public Boolean  ON_OFF;
        public Double   Ramp;
        /* Security and protection service */
        public Boolean  StealWarning;
        public Boolean  AccidentWarningP;
        public Integer  AccidentWarningML;
        public Boolean  TempWarning;
        public Double   EulerZ;
        public Double   EulerY;
        public Double   EulerX;
        public Double   dadtx;
        public Double   dadty;
        public Double   dadtz;
        /* Maintenance service */
        public Double   r;
        public Double   v;

        public void dispose(){
            this.NullInd = null;
            this.State = null;
            this.Acc_fren = null;
            this.ON_OFF = null;
            this.Ramp = null;
            this.StealWarning = null;
            this.AccidentWarningP = null;
            this.AccidentWarningML = null;
            this.TempWarning = null;
            this.EulerZ = null;
            this.EulerY = null;
            this.EulerX = null;
            this.dadtx = null;
            this.dadty = null;
            this.dadtz = null;
            this.r = null;
            this.v = null;
        }

    }

    /**
     *
     * @param Current Data of the vehicle for the instant processed
     * @param PreviousVeh Data of the vehicle for the last stored instants
     * @param PreviousProcessed Outputs produced on the previous instants
     * @return Output -- processed outcome of the algorithm
     */

    public static ProcessedData ProcessAlgorithm(VehData Current, LinkedList<VehData> PreviousVeh, LinkedList<ProcessedData> PreviousProcessed){
        /* Define output variable */
        ProcessedData Output = new ProcessedData();

        Output.NullInd = false;

        /* Pre-process of data */
        Double ACCX = Current.Acc.get(0);
        Double ACCY = Current.Acc.get(1);
        Double ACCZ = Current.Acc.get(2);
        Double MAGX = Current.Mag.get(0);
        Double MAGY = Current.Mag.get(1);
        Double MAGZ = Current.Mag.get(2);
        Double Lat  = Current.Lat;
        Double Lon  = Current.Lon;
        Double Temp = Current.Temp;
        //Double Bat  = Current.Bat;

        /* Define intermediate variables */
        double ACCrms = sqrt(ACCX*ACCX + ACCY*ACCY + ACCZ*ACCZ);

        /* Find state from ACC */
        if(abs(ACCX) > 0.2){ // Vehicle is stopped
            if( ACCrms > 0.5){
                Output.State = "Parada";
            }else{
                Output.State = "Estacionamiento";
            }
        }else{ // Vehicle is moving
            if( ACCrms > 0.5){
                Output.State = "Movimiento_ON";
            }else{
                Output.State = "Movimiento_OFF";
            }
        }

        /* Find Euler Angles */
        try{
            /* Use MATLAB Algorithm to convert from ACC and MAG to Euler angles */
            /* Create Class */
            MATLABEulerClass EulerClass = new MATLABEulerClass();
            /* Obtain results */
            Object[] MATClassResult = new Object[1];
            Object[] MATClassInput = new Object[6];
            MATClassInput[0] = ACCX;
            MATClassInput[1] = ACCY;
            MATClassInput[2] = ACCZ;
            MATClassInput[3] = MAGX;
            MATClassInput[4] = MAGY;
            MATClassInput[5] = MAGZ;
            EulerClass.VehData2Euler(MATClassResult, MATClassInput);
            MWNumericArray temp = (MWNumericArray) MATClassResult[0];

            /* Convert to doubles */
            Output.EulerZ = temp.getDouble(1);
            Output.EulerY = temp.getDouble(2);
            Output.EulerX = temp.getDouble(3);

            EulerClass.dispose();
        /* Catch exception if something fails when the class is created */
        } catch (MWException e) {
            e.printStackTrace();
            Output.NullInd = true;
            return Output;
        }

        /* Start process algorithm */
        /* Assistance to driving service */
        /* Compute if this instant the vehicle is accelerating or decelerating */
        if(ACCX > 0.2){
            /* Accelerating */
            Output.Acc_fren = 1;
        } else if(ACCX < -0.2){
            /* Decelerating */
            Output.Acc_fren = -1;
        } else{
            /* Still */
            Output.Acc_fren = 0;
        }
        /* Store as output if the vehicle is on/off */
        Output.ON_OFF = Output.State.equals("Parada") || Output.State.equals("Movimiento_ON");

        /* Compute Ramp for the instant being computed */
        Output.Ramp = tan(Output.EulerY*3.1416/180)*100;

        /* Security and protection service */
        /* Detection of robbery */
        /* If the vehicle is OFF and moving set a warning */
        Output.StealWarning = Output.State.equals("Movimiento_OFF");

        /* Detection of accident */
        /* Check position */
        Output.AccidentWarningP = false;
        if(abs(Output.EulerY) > 60 || abs(Output.EulerX) > 80){
            for (VehData next : PreviousVeh){
                if(next.Acc.get(0) > 2 || next.Acc.get(1) > 2 || next.Acc.get(2) > 2.5){
                    Output.AccidentWarningP = true;
                    break;
                }
            }
            for (ProcessedData next : PreviousProcessed){
                if(next.dadtx > 2 || next.dadty > 2 || next.dadtz > 2.5){
                    Output.AccidentWarningP = true;
                    break;
                }
            }
        }
        /* Check temperature */
        Output.TempWarning = Temp > 50;

        /* Check Acc and dAcc/dt */
        if(PreviousVeh.size() > 0){
            Output.dadtx = (ACCX-PreviousVeh.getLast().Acc.get(0))/((Current.time - PreviousVeh.getLast().time)/1000);
            Output.dadty = (ACCY-PreviousVeh.getLast().Acc.get(1))/((Current.time - PreviousVeh.getLast().time)/1000);
            Output.dadtz = (ACCZ-PreviousVeh.getLast().Acc.get(2))/((Current.time - PreviousVeh.getLast().time)/1000);
        } else{
            Output.dadtx = 0.0;
            Output.dadty = 0.0;
            Output.dadtz = 0.0;
        }
        /* Initialize value */
        Output.AccidentWarningML = 0;
        /* Check if Acc or dAcc/dt surpasses the limits */
        for (VehData next : PreviousVeh){
            if(next.Acc.get(0) > 3 || next.Acc.get(1) > 3 || next.Acc.get(2) > 3.5){
                Output.AccidentWarningML = 1;
                break;
            }
        }
        for (ProcessedData next : PreviousProcessed){
            if(next.dadtx > 3 || next.dadty > 3 || next.dadtz > 3.5){
                Output.AccidentWarningML = 1;
                break;
            }
        }
        /* Check if the data is inside the model */
        /* Find Euler Angles */
        try{
            /* Use MATLAB Algorithm to convert from ACC and MAG to Euler angles */
            /* Create Class */
            NoveltyClass ModelClass = new NoveltyClass();
            /* Obtain results */
            Object[] MATClassResult = ModelClass.Online_Novelty(1, ACCX, ACCY, ACCZ, Output.dadtx, Output.dadty, Output.dadtz);
            MWNumericArray temp = (MWNumericArray) MATClassResult[0];
            /* Convert to doubles */
            Output.AccidentWarningML += (int) temp.getDouble();

            ModelClass.dispose();

            /* Catch exception if something fails when the class is created */
        } catch (MWException e) {
            e.printStackTrace();
            Output.NullInd = true;
            return Output;
        }

        /* Maintenance service */
        /* Compute the distance travelled and mean velocity*/
        if(PreviousVeh.size() > 0){
            Output.r = getDistanceFromLatLonInKm(PreviousVeh.getLast().Lat, PreviousVeh.getLast().Lon, Lat, Lon);
            Output.v = Output.r / ((Current.time - PreviousVeh.getLast().time)/1000);
        } else{
            Output.r = 0.0;
            Output.v = 0.0;
        }
        /* Check battery cycle */


        /* Return the processed class */
        return Output;
    }

    public static Double getDistanceFromLatLonInKm(Double lat1, Double lon1, Double lat2, Double lon2) {
        int R = 6371000; // Radius of the earth in m
        double dLat = 3.1416/180*(lat2 - lat1);
        double dLon = 3.1416/180*(lon2 - lon1);
        double a = sin(dLat / 2) * sin(dLat / 2) + cos(3.1416/180*(lat1)) * cos(3.1416/180*(lat2)) * sin(dLon / 2) * sin(dLon / 2);
        double c = 2 * atan2(sqrt(a), sqrt(1 - a));
        return R * c; // Distance in m
    }

}
