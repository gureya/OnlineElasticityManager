function [ soa_value ] = secondOrderArima( data )
% ARIMA(2,0,0) = second-order autoregressive model
%   This model describes a system whose mean reversion takes place in a
%   sinusoidally oscillating fashion, like the motion of a mass on a mass
%   on a spring that is subjected to random shocks

Mdl = arima(2,0,0); % second-order autoregressive model

EstMdl = estimate(Mdl,data);
[yF,yMSE] = forecast(EstMdl,1,'Y0',data);

soa_value = yF;

end

