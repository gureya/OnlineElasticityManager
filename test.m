fil1 = 'Experimental-Data/7clientsV2.txt';
fid1 = fopen(fil1);

rLatencies = [];
rthp = [];
wthp = [];

if all(fgetl(fid1) == -1)
    fprintf('Empty file...No training data available');
else
    out = textscan(fid1, '%s','headerlines',0, 'delimiter', '\n');
    %celldisp(out);
    for a=1:cellfun(@length,out) %for every row
        row = str2num(out{1}{a});
        rtp = row(1);
        wtp = row(2);
        dsz = row(3);
        rl = row(4);
        wl = row(5);
        sla = row(6);
        
        rLatencies = [rLatencies;rl];
        rthp = [rthp;rtp];
        wthp = [wthp;wtp];
        
        if (sla == 1)
            hold on
            s1 = scatter(rtp,wtp,'x','g');
        else
            hold on
            s2 = scatter(rtp,wtp,'+','r');
        end
    end
    %display(rLatencies);
    display('Read Throughtput Statistics');
    display(min(rthp));
    display(max(rthp));
    display(mean(rthp));
    
    display('Write Throughtput Statistics');
    display(min(wthp));
    display(max(wthp));
    display(mean(wthp));
    
    display('Read Latency Statistics');
    display(min(rLatencies));
    display(max(rLatencies));
    display(mean(rLatencies));
    w = prctile(rLatencies, 67);
    display(w);
    
    box on
    xlabel('Read Throughput (ops/second/server)')
    ylabel('Write Throughput (ops/second/server)')
    title('Training Data')
    legend([s2 s1], 'Violate SLO', 'Satisfy SLO')
    hold off;
end