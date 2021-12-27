function [net,tr,y] = entrena_nn(TrainingExperiments3Level,TrainingTargets3Level,hiddenLayerSize);


x = TrainingExperiments3Level';
t = TrainingTargets3Level';
%hiddenLayerSize = 2; %las pruebas son con 8

clear net tr
net = patternnet(hiddenLayerSize);

net.divideParam.trainRatio = 0.8;  %70/100;
net.divideParam.valRatio =  0.10; %15/100;
net.divideParam.testRatio = 0.10; % 15/100;

% Train the Network
net.trainFcn = 'trainrp';
net.trainParam.epochs=500;
net.trainParam.max_fail=1500;
net.trainParam.min_grad=1e-10;

[net,tr] = train(net,x,t);

% Test the Network
y = net(x);

figure, plotconfusion(t,y)

end