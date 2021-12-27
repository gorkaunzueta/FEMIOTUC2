% Per tal de fer l'entrenament del model i la validacio comptem amb 400
% mostres d'estat saludable i 200 d'estat fallo, per tant utilitzarem 200
% de saludable i 100 de fallo per l'entrenament i el mateix per la
% validació

Healthy_DATA = readtable('Healthy_data_processed.csv');
Failure_DATA_1 = readtable('Failure_data1_processed.csv');
Failure_DATA_2 = readtable('Failure_data2_processed.csv');

N_DIM = 2;
Classes_test = [1 2]; 


% for i=1 : length(Classes_test) %--> N Classes
%     Targets1C(1+(nsamples*(i-1)) : nsamples*i , 1 ) = i; %--> Class ID Target
% end
% 
% % Targets for classify
% for i=1 : length(Classes_test) %--> N Classes
%     Targets_Class(1+(nsamples*(i-1)) : nsamples*i , i ) = 1; %--> Class ID Target
% end
% 
% for i=1 : length(Classes_test) %--> N Classes
%     Targets1C(1+(nsamples*(i-1)) : nsamples*i , 1 ) = i; %--> Class ID Target
% end



Targets1C(1 : 100 , 1 ) = 1;
Targets1C(101 : 200 , 1 ) = 2;
Targets1C(201 : 300 , 1 ) = 1;

Targets_Class(1 :100 , 1 ) =1;
Targets_Class(1 :100 , 2 ) =0;
Targets_Class(101 :200 , 1 ) =0;
Targets_Class(101 :200 , 2 ) =1;
Targets_Class(201 :300 , 1 ) =1;
Targets_Class(201 :300 , 2 ) =0;

DATA_saludable=table2array (Healthy_DATA);
DATA_fallo_1=table2array (Failure_DATA_1);
DATA_fallo_2=table2array (Failure_DATA_2);

DATA_training= [DATA_saludable(1:100,:) ; DATA_fallo_1(1:100,:) ; DATA_saludable(201:300,:)];
DATA_test= [DATA_saludable(101:200,:) ; DATA_fallo_2(1:100,:) ; DATA_saludable(301:400,:)];


[PCA_V,COEFF_V,latentV,explainedV] = FeatureReduction_PCA(DATA_training,Targets1C,N_DIM,Classes_test);

%Ja tenim les dades en dos dimensions (PCA_V) i la matriu per tal de
%transformar les 12 variables en 2 (COEFF_V) 




TrainModel = PCA_V;
D = TrainModel;

% novelty sets
samples=100;
t1 = ones(samples,1);
t0 = zeros(samples,1);
ta = [t1;t0;t1];
VTA = ta;

TAr = ta;

model = fitcsvm(D,Targets1C,'KernelFunction','RBF','KernelScale','auto','ClassNames',{'1','0'},'OutlierFraction',0.1);


Valid=DATA_test*COEFF_V(:,1:2);

n = 1500;
x1 = linspace(2*min(Valid(:,1))-2, max(Valid(:,1))+2,n);
x2 = linspace(2*min(Valid(:,2))-2, max(Valid(:,2))+2,n);
[X1,X2] = meshgrid(x1,x2);
XG = [X1(:),X2(:)];
[labels,scores] = predict(model,XG);

class = zeros(size(labels));
for cont = 1:length(labels)
    if labels{cont}==('1')
        class(cont,1) = 1;
    end 
end

% Plot and boundary
figure,
title('{\bf SVM setA}')
xlabel('PC1')
ylabel('PC2')
set(gca,'Color','w')%Background colour
hold on 
contour(X1,X2,reshape(class,size(X1,1),size(X2,1)),[1,1],'Color','k');
legend('off')
hold on


% Validation

[labels_val,scores_val] = predict(model,Valid);
cont = 1;
confusion = zeros(3,3);
[p,m] = size(confusion);
for cont = 1:length(Valid)
   if labels_val{cont} == ('1') && VTA(cont)== 1
    confusion(1,1) = confusion(1,1)+1;
    color = [0 0 1] ;
    plot(Valid(cont,1),Valid(cont,2),'Marker','.','LineStyle','none','Color',color,'MarkerSize',20);
    hold on
   elseif labels_val{cont} == ('1') && VTA(cont)== 0
    confusion(2,1) = confusion(2,1)+1;   
    color = [1 0 1];
    plot(Valid(cont,1),Valid(cont,2),'Marker','.','LineStyle','none','Color',color,'MarkerSize',20);
    hold on
   elseif labels_val{cont} == ('0') && VTA(cont) == 1
    confusion(1,2) = confusion(1,2)+1;   
    color = [1 1 0];
    plot(Valid(cont,1),Valid(cont,2),'Marker','*','LineStyle','none','Color',color,'MarkerSize',10);
    hold on
   elseif labels_val{cont} == ('0') && VTA(cont) == 0
    confusion(2,2) = confusion(2,2)+1;   
    color = [1 0 0];
    plot(Valid(cont,1),Valid(cont,2),'Marker','*','LineStyle','none','Color',color,'MarkerSize',10);
    hold on
   end
end
ax = gca;
ax.FontSize = 12;
ax.XAxis.Label.FontSize = 14;
ax.YAxis.Label.FontSize = 14;


confusion(3,1) = confusion(1,1)+confusion(2,1);
confusion(3,2) = confusion(1,2)+confusion(2,2);
confusion(1,3) = confusion(1,1)+confusion(1,2);
confusion(2,3) = confusion(2,1)+confusion(2,2);


confusion_per = zeros(2,2);
for i = 1:p-1
    for j = 1:m
        confusion_per(i,j) = confusion(i,j)/confusion(i,3);
    end
end
known = zeros(confusion(3,1),3);
a = 1;
for cont = 1:length(Valid)
   if labels_val{cont} == ('1')
    known(a,1) = TAr(cont);
    known(a,2) = Valid(cont,1);
    known(a,3) = Valid(cont,2);
    a=a+1; 
   end
end
total_acc = (confusion_per(1,1)+confusion_per(2,2))/2
SVM_A = known;
confusion_per

cont=0;
for i=1:200
   if labels_val{i} == ('1')
    cont=cont+1;
    
   end
end


