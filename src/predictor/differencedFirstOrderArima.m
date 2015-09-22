function [ dfoa_value ] = differencedFirstOrderArima( data )
% ARIMA(1,1,0) = differenced first-order autoregressive model
%   if the errors of a random walk model are autocorrelated, perhaps the
%   problem can be fixed by adding one lag of the dependent variable to the
%   prediction equation. This is a first-order autoregressive model with
%   one order of nonseasonal differencing and a constant term--i.e.,an
%   ARIMA(1,1,0) model

Mdl = arima(1,1,0); % differenced first-order autoregressive model

EstMdl = estimate(Mdl,data);
[yF,yMSE] = forecast(EstMdl,1,'Y0',data);

dfoa_value = yF;

end

