function [ rwa_value ] = randomWalkArima( data )
% ARIMA(0,1,0) = random walk
%   If the series Y is not stationary, the simplest possible model for it
%   is a random walk model

Mdl = arima(0,1,0); % random walk

EstMdl = estimate(Mdl,data);
[yF,yMSE] = forecast(EstMdl,1,'Y0',data);

rwa_value = yF;

end

