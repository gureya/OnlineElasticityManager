function [ foa_value ] = firstOrderArima( data )
%ARIMA(1,0,0) = first-order autoregressive model (if the series is stationary and autocorrelated) arma method Summary of this function goes here
%   Detailed explanation goes here
Mdl = arima(1,0,0); % exponential smoothing

EstMdl = estimate(Mdl,data);
[yF,yMSE] = forecast(EstMdl,1,'Y0',data);

foa_value = yF;

end

