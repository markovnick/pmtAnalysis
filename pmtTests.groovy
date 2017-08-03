import org.jlab.evio.clas12.*;
import java.io.*;
import org.jlab.clas.physics.*;
import org.jlab.clas12.physics.*;
import org.root.histogram.*;
import org.root.pad.*;
import org.root.data.*;
import org.root.func.*;	
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.detector.decode.*;
import org.jlab.coda.jevio.ByteDataTransformer;
import org.jlab.coda.jevio.CompositeData;
import org.jlab.coda.jevio.DataType;
import org.jlab.coda.jevio.EvioException;
import org.jlab.coda.jevio.EvioNode;
import org.jlab.detector.decode.DetectorDataDgtz.ADCData;
import org.jlab.detector.decode.DetectorDataDgtz.TDCData;
import org.jlab.io.evio.EvioDataEvent;
import org.jlab.io.evio.EvioSource;
import org.jlab.io.evio.EvioTreeBranch;
import org.jlab.utils.data.DataUtils;
import org.jlab.detector.base.DetectorDescriptor;
import org.jlab.detector.base.DetectorType;
import org.root.histogram.*;
import org.root.pad.*;
import org.root.data.*;
import org.root.func.*;	
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.data.TDirectory;
import org.jlab.groot.graphics.EmbeddedCanvasTabbed;

import javax.swing.JFrame;
import org.jlab.groot.graphics.EmbeddedCanvas;

import java.io.IOException;
import java.io.PrintWriter;
import org.clas.viewer.AnalysisMonitor;
import org.jlab.clas.physics.Particle;
import org.jlab.groot.data.H1F;
import org.jlab.groot.data.H2F;
import org.jlab.groot.group.DataGroup;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.groot.ui.TCanvas;

import org.jlab.groot.fitter.DataFitter;
import org.jlab.groot.group.DataGroup;
import org.jlab.groot.math.F1D;


int left = 440;
int right = 480;
H1F spectrumPed = new H1F("spectrumPed", right-left, left, right);
spectrumPed.setTitleX("Signal, channels");
spectrumPed.setTitle("PMT Signal for pedestal");


H1F spectrum = new H1F("spectrum", 5000, 0,5000);
spectrum.setTitleX("Signal, channels");
spectrum.setTitle("PMT Signal");

H1F spectrumLED = new H1F("spectrumLED", 5000, 0,5000);
spectrumLED.setTitleX("Signal around LED, channels");
spectrumLED.setTitle("PMT Signal");

H1F ADCspectrum = new H1F("ADCspectrum",  200, 0, 200);
ADCspectrum.setTitle("Integrated spectrum of the PMT signal");
ADCspectrum.setTitleX("Time, 4*ns");



double pedestalFit = 0;
int darkSignalCounter = 0;
double darkSignal = 0;

F1D pedF = new F1D("pedF","[amp]*gaus(x,[mean],[sigma])", 450, 470);
pedF.setParameter(0, 10000000);
pedF.setParameter(2, 2.65);
pedF.setLineColor(2);
pedF.setLineWidth(2);

int startSingleEvent = 100;
List<H1F> sEventH = new ArrayList<H1F>();
for (int t = 0; t < 100; t++){
	sEventH.add(new H1F("sEventH" + t, 200, 0, 200));
	sEventH.get(t).setTitleX("Time, 4*ns");
	int c = startSingleEvent + t;
	sEventH.get(t).setTitle("Spectrum of event" + c);
}
//println( args.size() );
int eCounter = 0;
EvioSource reader = new EvioSource();
runnum = "5302";
if( args.size() != 0 ){
  	runnum = args[0];
}
def stats = new File("txt/chargeAndCurrent_run_" + runnum + ".txt");
reader.open("data/pmttest_00"+ runnum + ".evio.0");
//reader.open("pmttest_005605.evio.0");
CodaEventDecoder decoder = new CodaEventDecoder();
//DetectorEventDecoder detectorDecoder = new DetectorEventDecoder();

while(reader.hasEvent()==true){
	eCounter++;
	
	// If you want to quickly check something, you can look at the first 10k events instead of entire 200k 
	//if( eCounter > 10000 ){ break; }

	EvioDataEvent event = (EvioDataEvent) reader.getNextEvent();
	List<DetectorDataDgtz>  rawEntries = decoder.getDataEntries(event);
	for(DetectorDataDgtz d: rawEntries){
		if(d.getDescriptor().getCrate()==6&&d.getDescriptor().getChannel()==0){
			if(d.getADCSize()>0){
				short[] pulse = d.getADCData(0).getPulseArray();
				if (eCounter < startSingleEvent + 4 + 100&& eCounter > startSingleEvent + 3){
					for(int i = 0; i < pulse.length; i++){
						sEventH[eCounter - startSingleEvent - 4].fill(i, pulse[i]);
					}
				}
				for(int i = 0; i < pulse.length; i++){
					if (i > 120 && i < 160){
					   spectrumPed.fill(pulse[i]);
					}
                                        spectrum.fill(pulse[i]);
					if (i < 38 && i > 25){
					spectrumLED.fill(pulse[i]);
					} 
					darkSignal = darkSignal + pulse[i];
					darkSignalCounter++;
					ADCspectrum.fill(i, pulse[i]);
				}
			}
		}
	}
}

JFrame frame1 = new JFrame();
EmbeddedCanvas c1 = new EmbeddedCanvas();
frame1.setSize(700,1000);
frame1.add(c1);

c1.divide(2, 3);
c1.cd(1);
int max = spectrumPed.getMaximumBin() + left;
System.out.println("max: " + max);

pedF.setParameter(1, max);
DataFitter.fit(pedF, spectrumPed, "E");
pedestalFit = pedF.getParameter(1);
frame1.add(c1);
System.out.println("sigma: " + pedF.getParameter(2));

c1.draw(spectrumPed);
c1.draw(pedF, "same");
c1.cd(0);
c1.draw(ADCspectrum);
c1.cd(2);
c1.draw(spectrum);
c1.cd(3);
c1.draw(spectrumLED);
c1.cd(4);
c1.getPad(4).getAxisY().setLog(true);
c1.draw(spectrum);
c1.cd(5);
c1.getPad(5).getAxisY().setLog(true);
c1.draw(spectrumLED);
frame1.setVisible(true);

String fToSave = "img/pmtAnalysis" +  runnum + ".png";
c1.save(fToSave);

//TCanvas c2 = new TCanvas("c2",600,1200);
JFrame frame2 = new JFrame();
EmbeddedCanvas c2 = new EmbeddedCanvas();
frame2.setSize(800,500);
frame2.add(c2);
frame2.setVisible(true);

c2.divide(10, 10);
for (int t = 0; t < 100; t++){
	c2.cd(t - 0);
	c2.draw(sEventH.get(t));
}


double rangeSetting = 1;
int irunnum = runnum as int;
if( irunnum < 5359){ rangeSetting = 0.5; }
println( rangeSetting );

double UnitOfIn = 1/50.0;

double darkRealSignal = darkSignal - pedestalFit*darkSignalCounter;

double darkCharge = darkRealSignal*UnitOfIn*rangeSetting/4096*4e-9;
double darkTime = darkSignalCounter*4e-9;
double darkCurrent = darkCharge/darkTime;

System.out.println();
System.out.println();
System.out.println("============ Results For Run : " + runnum + " =============");
System.out.println();
System.out.println("Ped position: " +  pedestalFit);
System.out.println("Dark signal:  " + darkSignal);
System.out.println("Dark counter: " + darkSignalCounter);
System.out.println("Dark time:    " + darkTime + " s");
System.out.println();
System.out.println("Dark charge:  " + darkCharge*1e6 + " uC");
System.out.println("Dark current: " + darkCurrent*1e9 + " nA");
System.out.println();
System.out.println();

stats << darkCharge*1e6 << "\t" << darkCurrent*1e9 << "\n";
