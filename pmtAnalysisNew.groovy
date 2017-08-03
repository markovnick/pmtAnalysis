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
//import org.jlab.groot.data.TLine;
import org.jlab.groot.group.DataGroup;
import org.jlab.io.base.DataBank;
import org.jlab.io.base.DataEvent;
import org.jlab.groot.ui.TCanvas;
//import org.jlab.groot.ui.Line;

import org.jlab.groot.fitter.DataFitter;
import org.jlab.groot.group.DataGroup;
import org.jlab.groot.math.F1D;

import java.util.Collections;


runnum = "5464";
if( args.size() != 0 ){
  runnum = args[0];
}
System.out.println("run number " + runnum);
int left = 440;
int right = 480;
int fitleft = 450;
int fitright = 480;
if( runnum == "5440" || runnum == "5441" ){
  // This run was an anomaly
  left = 420;
  right = 550;
  
  // Left Gaussian
  //fitleft = 430;
  //fitright = 442;
  
  // Right Gaussian
  fitleft = 500;
  fitright = 520;
}

H1F spectrumPed, spectrum, spectrumLED, ADCspectrum;



//void initHists(){
  spectrumPed = new H1F("spectrumPed", right-left, left, right);
  spectrumPed.setTitleX("Signal, channels");
  spectrumPed.setTitle("PMT Signal for pedestal");
  
  spectrum = new H1F("spectrum", 5000, 0,5000);
  spectrum.setTitleX("Signal, channels");
  spectrum.setTitle("PMT Signal");

  spectrumLED = new H1F("spectrumLED", 5000, 0,5000);
  spectrumLED.setTitleX("Signal around LED, channels");
  spectrumLED.setTitle("PMT Signal");

  ADCspectrum = new H1F("ADCspectrum",  200, 0, 200);
  ADCspectrum.setTitle("Integrated spectrum of the PMT signal");
  ADCspectrum.setTitleX("Time, 4*ns");
//}




double getPedestal(H1F freqHist, int bin0){
  // Get the pedestal from frequency histogram 

  int pedbin = freqHist.getMaximumBin();
  return pedbin + bin0; // x[bin] = bin * (dbin/nbins) + x[0];
}

double[] getRealSignal(){}
double getCharge(){}
double getCurrent(){}



int startSingleEvent = 100;
List<H1F> sEventH = new ArrayList<H1F>();
for (int t = 0; t < 100; t++){
  sEventH.add(new H1F("sEventH" + t, 200, 0, 200));
  sEventH.get(t).setTitleX("Time, 4*ns");
  int c = startSingleEvent + t;
  sEventH.get(t).setTitle("Spectrum of event" + c);
}





EvioSource reader = new EvioSource();
reader.open("evioMarkov/pmttest_00"+ runnum + ".evio.0");

CodaEventDecoder decoder = new CodaEventDecoder();

int temp_ped, plength;
int eCounter = 0;
int darkSignalCounter = 0;
int darkSignalCounter3 = 0;
int darkSignalCounter5 = 0;
int darkSignalCounter7 = 0;
int darkSignalCounter10 = 0;
int darkSignalCounter15 = 0;
int darkSignalCounter25 = 0;

double pedestalFit = 0;
double darkSignal = 0;
double darkSignal3 = 0;
double darkSignal5 = 0;
double darkSignal7 = 0;
double darkSignal10 = 0;
double darkSignal15 = 0;
double darkSignal25 = 0;
double darkSignal2 = 0;

double ped = 0;
H1F temp_spectrumPed, temp_ADCspectrum , temp_spectrum , temp_spectrumLED , h_temp_ped;
int reinitHists(){
  // Reinitilize Histograms for each event
  return 2;
}
//initHists();
while(reader.hasEvent()==true){
  eCounter++;

  // If you want to quickly check something, you can look at the first 1k events instead of entire 200k 
  if( eCounter > 10000 ){ break; }

  EvioDataEvent event = (EvioDataEvent) reader.getNextEvent();
  List<DetectorDataDgtz>  rawEntries = decoder.getDataEntries(event);
  for(DetectorDataDgtz d: rawEntries){
    if(d.getDescriptor().getCrate()==6&&d.getDescriptor().getChannel()==0){ //?
      if(d.getADCSize()>0){
        if( eCounter % 25000 == 0 ){ println eCounter + " / 200 k" ; } 
        // Reinitializes "temp" hists every event
        temp_spectrumPed = new H1F("temp_spectrumPed", right-left, left, right);
        temp_ADCspectrum = new H1F("temp_ADCspectrum",  200, 0, 200);
        temp_spectrum = new H1F("temp_spectrum", 5000, 0,5000);
        temp_spectrumLED = new H1F("temp_spectrumLED", 5000, 0,5000);
        h_temp_ped = new H1F("temp_ped",  200, 0, 200);
        
        short[] pulse = d.getADCData(0).getPulseArray();
        plength = pulse.length;
        if (startSingleEvent + 3  < eCounter && eCounter < startSingleEvent + 4 + 100 ){ //?
	  for(int i = 0; i < pulse.length; i++){
	      sEventH[eCounter - startSingleEvent - 4].fill(i, pulse[i]);
	        }
		}
        // Find pedastal in this loop
        for(int ii = 0; ii < pulse.length; ii++){
          if( 25 < ii && ii < 38 ){
            // LED should be pulsed in this time window
            temp_spectrumLED.fill(pulse[ii]);
            spectrumLED.fill(pulse[ii]);
          } 
          //else if( 120 < ii && ii < 160 ){ 
            // Fill occurrence frequency histograms to find pedestal
            temp_spectrumPed.fill(pulse[ii]);
            spectrumPed.fill(pulse[ii]);
          //}
          // Fill signal vs time
          temp_ADCspectrum.fill(ii, pulse[ii]);
          ADCspectrum.fill(ii, pulse[ii]);
          
          // Fill occurrence frequency histograms to plot 
          spectrum.fill(pulse[ii]);
          temp_spectrum.fill(pulse[ii]);
        }
        
        temp_ped = getPedestal(temp_spectrumPed, left);
        ped += temp_ped;

        for( int ii = 0; ii < pulse.length; ii++){
          h_temp_ped.fill(ii, temp_ped);
          // All of this below can be done before this loop but it's
          // illustrative of what the dark signal is
          double diff  = pulse[ii] - temp_ped; 
          darkSignal  += diff;
 	  if (pulse[ii] > temp_ped + 3){
             darkSignal3 += diff;
             darkSignalCounter3++;
          }
	  if (pulse[ii] > temp_ped + 5){
             darkSignal5 += diff;
	     darkSignalCounter5++;
	  }
	   if (pulse[ii] > temp_ped + 7){
             darkSignal7 += diff;
             darkSignalCounter7++;
          }

	  if (pulse[ii] > temp_ped + 10){
             darkSignal10 += diff;
             darkSignalCounter10++;
	       }
	  if (pulse[ii] > temp_ped + 15){
             darkSignal15 += diff;
             darkSignalCounter15++;
         }
         if (pulse[ii] > temp_ped + 25){
             darkSignal25 += diff;
             darkSignalCounter25++;
         }

	  darkSignal2 += pulse[ii];
          darkSignalCounter++;
        }
      }
    }
  }
}

int max = getPedestal(spectrumPed, left);
System.out.println("max: " + max);
double ped2 = 0;

F1D pedF = new F1D("pedF","[amp]*gaus(x,[mean],[sigma])", fitleft, fitright);
pedF.setParameter(0, 10000000);
pedF.setParameter(2, 2.65);
pedF.setLineColor(2);
pedF.setLineWidth(2);
if( runnum == "5440" ){
  // Specific Study
  //// Left Peak
  //max = 440;
  //// Right Peak
  //max = 505;
  //pedF.setParameter(1, max);
  //DataFitter.fit(pedF, spectrumPed, "E");
  //pedestalFit = pedF.getParameter(1);

  // After study, use this as the pedestal
  pedestalFit = (504.34976090848767 + 438.70689142013714)/2; 
}
else{
  pedF.setParameter(1, max);
  DataFitter.fit(pedF, spectrumPed, "E");
  pedestalFit = pedF.getParameter(1);
}
ped2 = pedestalFit* eCounter;

H1F h_ped = new H1F("ped",  200, 0, 200);
H1F h_ped2 = new H1F("ped2",  200, 0, 200);
for(int ii = 0; ii < plength; ii++){
  h_ped.fill(ii, ped);
  h_ped2.fill(ii, ped2);
}
double pctdiff = 100*(ped2 - ped)/ped;
println "percent diff : " + pctdiff;



// Constants
double timePerChannel= 4e-9; // in seconds
double rangeSetting = 1; // this is Voltage for 4096 steps
int irunnum = runnum as int;
if( irunnum < 5359){ rangeSetting = 0.5; }
//double UnitOfIn = 1/50.0; //?
//double R = 1/UnitOfIn; // Resistance in Ohm
double R = 50; // Resistance in Ohm
double toVoltage = rangeSetting/4096; // multiply FADC to this to get voltage in V: rangeSetting = 4096 steps in FADC
double toCharge = toVoltage/R * timePerChannel; // multiply FADC to this to get Charge: (V/R)*time

// Calculate dark current
double darkTime = darkSignalCounter * timePerChannel;
double darkRealSignal = darkSignal;
double darkCharge = darkRealSignal * toCharge;
double darkCurrent = darkCharge/darkTime;

double darkRealSignal2 = darkSignal2 - pedestalFit * darkSignalCounter;;
double darkCharge2 = darkRealSignal2 * toCharge;
double darkCurrent2 = darkCharge2/darkTime;

double darkRealSignal3 = darkSignal3;
double darkCharge3 = darkRealSignal3 * toCharge;
double darkCurrent3 = darkCharge3/darkTime;

double darkRealSignal5 = darkSignal5;
double darkCharge5 = darkRealSignal5 * toCharge;
double darkCurrent5 = darkCharge5/darkTime;

double darkRealSignal7 = darkSignal7;
double darkCharge7 = darkRealSignal7 * toCharge;
double darkCurrent7 = darkCharge7/darkTime;

double darkRealSignal10 = darkSignal10 ;
double darkCharge10 = darkRealSignal10 * toCharge;
double darkCurrent10 = darkCharge10/darkTime;

double darkRealSignal15 = darkSignal15;
double darkCharge15 = darkRealSignal15 * toCharge;
double darkCurrent15 = darkCharge15/darkTime;

double darkRealSignal25 = darkSignal25;
double darkCharge25 = darkRealSignal25 * toCharge;
double darkCurrent25 = darkCharge25/darkTime;



//def runranges = [ 5335, 5371, 5386, 5401, 5414, 5426, 5438, 5450, 5462, 5475 5487 ];
def runranges = [ 5371, 5386, 5401, 5414, 5426, 5438, 5450, 5462, 5475, 5487 ];
def pmt_ids = [ 16762, 16763, 16750, 16755, 16769, 16745, 16756, 16772, 16765, 16776, 16764 ];  
def nomVoltages = [ 2.094, 0, 2.36, 0, 2.125, 2.29, 2.178, 2.204, 2.212, 2.392, 2.173 ]; 
def nomCurrents = [ 155,   0, 22.6, 0, 17.35, 26, 140.2, 223, 15.6, 54, 56 ];


System.out.println("run number " + runnum);

int pmtid = pmt_ids[0];
float nomV  = nomVoltages[0];
float nomI  = nomCurrents[0];

int jj = 0;
while( (runnum as Integer) > runranges[jj] && jj < runranges.size){
  jj++;
  pmtid = pmt_ids[jj];
  nomV  = nomVoltages[jj];
  nomI  = nomCurrents[jj];
}
// Print report to screen
System.out.println();
System.out.println();
System.out.println("============ Results For Run : " + runnum + " (PMT number : " + pmtid + ") =============");
System.out.println();
System.out.println("Dark counter: " + darkSignalCounter);
System.out.println("Dark time:    " + darkTime + " s");
System.out.println("------------ Pedestal Position  -------------");
System.out.println();
System.out.println("Final Event: " +  temp_ped);
System.out.println("Cumulative Gaussian Fit: " + pedestalFit );
System.out.println();
System.out.println("------------ Pedestal Gaussian fit  -------------");
System.out.println();
System.out.println();
System.out.println("------------ Event by event cumulative  -------------");
System.out.println();
System.out.println("Ped position : " +  ped);
System.out.println("Dark signal :  " + darkSignal);
System.out.println("Dark charge :  " + darkCharge*1e6 + " uC");
System.out.println("Dark current: " + darkCurrent*1e9 + " nA");
System.out.println();
System.out.println("------------ Global Gaussian Fit  -------------");
System.out.println();
System.out.println("Ped position : " +  ped2);
System.out.println("Dark signal :  " + darkSignal2);
System.out.println("Dark time :  " + darkTime);

System.out.println("Dark charge :  " + darkCharge2*1e6 + " uC");
System.out.println("Dark current: " + darkCurrent2*1e9 + " nA");
System.out.println();
System.out.println();

System.out.println("------------ Global Gaussian Fit + 3 Step -------------");
System.out.println();
System.out.println("Dark signal :  " + darkSignal3);
System.out.println("Dark charge :  " + darkCharge3*1e6 + " uC");
System.out.println("Dark current: " + darkCurrent3*1e9 + " nA");

System.out.println("------------ Global Gaussian Fit + 5 Step -------------");
System.out.println();
System.out.println("Dark signal :  " + darkSignal5);
System.out.println("Dark charge :  " + darkCharge5*1e6 + " uC");
System.out.println("Dark current: " + darkCurrent5*1e9 + " nA");

System.out.println("------------ Global Gaussian Fit + 6 Step -------------");
System.out.println();
System.out.println("Dark signal :  " + darkSignal7);
System.out.println("Dark charge :  " + darkCharge7*1e6 + " uC");
System.out.println("Dark current: " + darkCurrent7*1e9 + " nA");



System.out.println("------------ Global Gaussian Fit + 10 Step -------------");
System.out.println();
System.out.println("Dark signal :  " + darkSignal10);
System.out.println("Dark charge :  " + darkCharge10*1e6 + " uC");
System.out.println("Dark current: " + darkCurrent10*1e9 + " nA");

System.out.println("------------ Global Gaussian Fit + 15 Step -------------");
System.out.println();
System.out.println("Dark signal :  " + darkSignal15);
System.out.println("Dark charge :  " + darkCharge15*1e6 + " uC");
System.out.println("Dark current: " + darkCurrent15*1e9 + " nA");


System.out.println("------------ Global Gaussian Fit + 25 Step -------------");
System.out.println();
System.out.println("Dark signal :  " + darkSignal25);
System.out.println("Dark charge :  " + darkCharge25*1e6 + " uC");
System.out.println("Dark current: " + darkCurrent25*1e9 + " nA");


System.out.println();
System.out.println();



// Print summary to text files
def stats = new File("textMarkov/chargeAndCurrent_run_" + runnum + ".txt");
//def results = new File("txt/results.txt");
//def stats2 = new File("txt/1_chargeAndCurrent_run_" + runnum + ".txt");

stats<<  runnum << " " << pmtid << "\n";  
stats<< "0 " << darkCurrent*1e9 << "\n";
stats<< "3 " << darkCurrent3*1e9 << "\n";
stats<<	"5 " <<	darkCurrent5*1e9	<<  "\n";
stats<< "7 " << darkCurrent7*1e9        <<  "\n";
stats<<	"10 " << darkCurrent10*1e9	<<  "\n";
stats<<	"15 " << darkCurrent15*1e9	<<  "\n";
stats<<	"25 " << darkCurrent25*1e9	<<  "\n";


//stats << darkCharge*1e6 << "\t" << darkCurrent*1e9 << " " << pctdiff << "\n";
//stats2 << darkCharge2*1e6 << "\t" << darkCurrent2*1e9 << " " << pctdiff << "\n"; 
//results << runnum << "\t" << pmtid << "\t" + nomV + " \t" + nomI + "\t\t" << darkCurrent*1e9 << "\t\t" << darkCurrent2*1e9 << "\n";


// Draw and Print
JFrame frame1 = new JFrame();
EmbeddedCanvas c1 = new EmbeddedCanvas();
frame1.setSize(1400,1000);
frame1.add(c1);

c1.divide(4, 3);
frame1.add(c1);

c1.cd(0);
h_temp_ped.setLineColor(2);
//c1.getPad(0).getAxisY().setLog(true);
c1.draw(temp_ADCspectrum);
c1.draw(h_temp_ped, "same");

c1.cd(1);
c1.draw(temp_spectrumPed);

c1.cd(2);
//c1.getPad(2).getAxisY().setLog(true);
h_ped.setLineColor(2);
h_ped2.setLineColor(4);
c1.draw(ADCspectrum);
c1.draw(h_ped, "same");
//h_ped2.setLineWidth(5);
c1.draw(h_ped2, "same");

c1.cd(3);
c1.draw(spectrumPed);
c1.draw(pedF, "same");

c1.cd(4);
c1.draw(temp_spectrum);

c1.cd(5);
c1.draw(temp_spectrumLED);

c1.cd(6);
c1.draw(spectrum);

c1.cd(7);
c1.draw(spectrumLED);

c1.cd(8);
c1.getPad(8).getAxisY().setLog(true);
c1.draw(temp_spectrum);

c1.cd(9);
c1.getPad(9).getAxisY().setLog(true);
c1.draw(temp_spectrumLED);

c1.cd(10);
c1.getPad(10).getAxisY().setLog(true);
c1.draw(spectrum);

c1.cd(11);
c1.getPad(11).getAxisY().setLog(true);
c1.draw(spectrum);

frame1.setVisible(true);

String fToSave = "imagesMarkov/pmtAnalysis" +  runnum + ".png";
//c1.save(fToSave);


JFrame frame2 = new JFrame();
EmbeddedCanvas c2 = new EmbeddedCanvas();
frame2.setSize(400,400);
frame2.add(c2);

c2.divide(1, 1);
frame2.add(c2);

c2.cd(0);
c2.getPad(0).getAxisY().setLog(true);
c2.draw(spectrum);
frame2.setVisible(true);

c2.save(fToSave);

//TCanvas c2 = new TCanvas("c2",600,1200);

//c2.divide(10, 10);
//for (int t = 0; t < 100; t++){
//    c2.cd(t - 0);
//    c2.draw(sEventH.get(t));
//}


//if (startSingleEvent + 3  < eCounter && eCounter < startSingleEvent + 4 + 100 ){ //?
//  for(int i = 0; i < pulse.length; i++){
//    sEventH[eCounter - startSingleEvent - 4].fill(i, pulse[i]); 
//  }
//}

