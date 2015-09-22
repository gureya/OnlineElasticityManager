function [ es_value ] = exponentialSmoothing( data)
% ARIMA(0,1,1)
% simple exponential smoothing - non-stationary time series (ones that exhibit noisy fluctuations
% around a slowly-varying mean).

%Mdl = arima('Constant',0,'D',1,'Seasonality',12,...
%    'MALags',1,'SMALags',12);

Mdl = arima(0,1,1); % exponential smoothing

EstMdl = estimate(Mdl,data);
[yF,yMSE] = forecast(EstMdl,1,'Y0',data);

es_value = yF;

end

