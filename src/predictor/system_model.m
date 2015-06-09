function[ w, b ] = system_model( feature1, feature2, feature3, trainlabels )
% Train the model and get the primal variables w, b from the model
% Credits to this tutorial
% http://openclassroom.stanford.edu/MainFolder/DocumentPage.php?course=MachineLearning&doc=exercises/ex7/ex7.html&quot
% Libsvm options
% -s 0 : classification
% -t 0 : linear kernel
% -c somenumber : set the cost
% WARNING: using -h 0 may be faster

trainfeatures = [feature1,feature2,feature3];

X = trainfeatures;
y = trainlabels;

model = svmtrain(y, X, '-s 0 -t 0 -c 1 -h 0');

w = model.SVs' * model.sv_coef;
b = -model.rho;
if (model.Label(1) == -1)
    w = -w; b = -b;
end
end
    
    