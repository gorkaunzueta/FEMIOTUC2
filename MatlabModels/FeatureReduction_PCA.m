function [FR2,COEFF1,latent1,explained1] = FeatureReduction_PCA(FeatureN,Targets1C,N_DIM,Classes)
%%% covariance matrix and eigenvectors %%%
V = cov(FeatureN);
[COEFF1,latent1,explained1] = pcacov(V);
proj = FeatureN*COEFF1(:,1:N_DIM);

% Project the data set on the space spanned by the column vectors of V
 %Create a latent space with the new projections

C = {'b','r','m','c','g','r','m','c'};
M = {'^','o','*','s','s','*','o','^'};
L = {'Hlt','Fault 1','Fault 2','Fault 3','Fault 4'};
CM = ['b^' 'ro' 'm*' 'cs'];
D = {'Dist. Norm. Y','Dist. Norm. Z'};
PC = {' PC1 ',' PC2 '};

    figure

    for j=1 : length(Classes)
        
        if N_DIM == 3
            
            idxC = find( Targets1C == j );
            scatter3(proj(idxC,1),proj(idxC,2),proj(idxC,3),'marker',M{Classes(j)},'MarkerEdgeColor',C{Classes(j)},'LineWidth',1)
        
        else
        if N_DIM == 2
        
            idxC = find( Targets1C == j );
            scatter(proj(idxC,1),proj(idxC,2),'marker',M{Classes(j)},'MarkerEdgeColor',C{Classes(j)},'LineWidth',1)
    
        end
        end
        
        hold on
        
    end
       
%     grid on
    legend(L(Classes))
    
xhandle = xlabel(sprintf('1st Principal component'));
set(xhandle,'FontName','Times New Roman');
set(xhandle,'FontWeight','Bold');
set(xhandle,'FontSize',10);

yhandle = ylabel(sprintf('2nd Principal component'));
set(yhandle,'FontName','Times New Roman');
set(yhandle,'FontWeight','Bold');
set(yhandle,'FontSize',10);

set(gca,'FontName','Times New Roman');
set(gca,'FontSize',10);
    
    FR2 = proj;
    
end