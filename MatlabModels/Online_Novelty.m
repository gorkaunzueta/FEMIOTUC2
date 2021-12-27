

function VALID = Online_Novelty(ACCX,ACCY,ACCZ,dACCXdt,dACCYdt,dACCZdt)
    %% Load data 
    load('model.mat','COEFF_V','model');
    %% Change dimensions
    X=[ACCX ACCY ACCZ dACCXdt dACCYdt dACCZdt]*COEFF_V(:,1:2);
    %% Predict result
    VALID = str2double(predict(model,X));
end





