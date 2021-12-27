%%% Data Normalization %%%

function [y] = data_normal2(x1,x2)

n=length(x1);

data_mean=mean(x2);
data_std=std(x2);

y=x1-data_mean;
y=y/(data_std + (data_std==0));
end