clear all
addpath('C:\Users\Josep\Desktop\UNI\TFG\matlab\influxdb-matlab-master\influxdb-matlab-master\influxdb-client');

URL = 'http://51.103.49.181:8086';
USER = 'femiot';
PASS = 'mcia1234';
DATABASE = 'pruebadb';
influxdb = InfluxDB(URL, USER, PASS, DATABASE);
 
fallo=0;
aux=0;
tic;

 while 1   
    
    if(toc>60)
        fallo=1;
        tic;
        aux=1;
    end
    
    if(toc>40 && aux==1)
        fallo=0;
        tic;
        aux=0;
    end       
        
    if(fallo==1) 
        x=round(2+199*rand);% nombre aleatori 2-201
    else
        x=round(202+399*rand); % nombre aleatori 202-601
    end   
    
    x2=num2str(x);
    
    X_RMS= xlsread("datos_vibracion.xlsx",strcat("A",x2,":A",x2));
    X_MEAN= xlsread("datos_vibracion.xlsx",strcat("B",x2,":B",x2));
    X_MAX= xlsread("datos_vibracion.xlsx",strcat("C",x2,":C",x2));
    X_MIN= xlsread("datos_vibracion.xlsx",strcat("D",x2,":D",x2));
    Y_RMS= xlsread("datos_vibracion.xlsx",strcat("E",x2,":E",x2));
    Y_MEAN= xlsread("datos_vibracion.xlsx",strcat("F",x2,":F",x2));
    Y_MAX= xlsread("datos_vibracion.xlsx",strcat("G",x2,":G",x2));
    Y_MIN= xlsread("datos_vibracion.xlsx",strcat("H",x2,":H",x2));
    Z_RMS= xlsread("datos_vibracion.xlsx",strcat("I",x2,":I",x2));
    Z_MEAN= xlsread("datos_vibracion.xlsx",strcat("J",x2,":J",x2));
    Z_MAX= xlsread("datos_vibracion.xlsx",strcat("K",x2,":K",x2));
    Z_MIN= xlsread("datos_vibracion.xlsx",strcat("L",x2,":L",x2));   
   
    series = Series('dataset_vibracio').tags('user', 'josep').fields('X_RMS',X_RMS,'X_MEAN',X_MEAN,...
    'X_MAX',X_MAX,'X_MIN',X_MIN,'Y_RMS',Y_RMS,'Y_MEAN',Y_MEAN,'Y_MAX',Y_MAX,'Y_MIN',Y_MIN,'Z_RMS',...
    Z_RMS,'Z_MEAN',Z_MEAN,'Z_MAX',Z_MAX,'Z_MIN',Z_MIN,'VALID',2).time(datetime('now', 'TimeZone', 'local'));
   
    influxdb.writer().append(series).execute();   
 end
    
 
 