{
	
	gROOT->Reset();
	TStyle *st1 = new TStyle("st1","my style");
	st1->SetOptStat(0);
	st1->SetPalette(1, 0);
	st1->SetOptTitle(0);
	st1->SetDrawBorder(0);
	st1->SetAxisColor(1, "x");
	st1->SetAxisColor(1, "y");
//  st1->SetBarOffset(2);
 // st1->SetBarWidth(2);
  //  st1->SetCanvasColor(25);
  //  st1->SetPadColor(25);
	st1->SetFrameBorderMode(0);
  //  st1->SetFrameFillColor(23);
	gStyle->SetCanvasColor(1);
	gStyle->SetFrameFillColor(0);
	gStyle->SetPadTopMargin(0.0);
	gStyle->SetPadBottomMargin(0.1);
	gStyle->SetPadLeftMargin(0.1);
	gStyle->SetPadRightMargin(0.0);
	gStyle->SetOptStat(0);

	st1->SetErrorX(0);

  //st1->SetGridColor(1);  
	st1->SetLabelFont(12, "x");
	st1->SetLabelFont(12, "y");


	st1->SetLabelColor(1, "y");
	st1->SetLabelColor(1, "x");

	st1->SetLabelSize(0.06, "y");
	st1->SetLabelSize(0.06, "x");
	st1->SetNdivisions(103, "x");
	st1->SetNdivisions(505, "y");

	st1->cd();
	gROOT->ForceStyle();
	int runnumber = 5475;
	TLatex xCaption;

	xCaption.SetTextSize(0.05);
	xCaption.SetTextFont(12);
	xCaption.SetNDC();
	
	ifstream a(Form("chargeAndCurrent_run_%d.txt", runnumber));
	cout << Form("chargeAndCurrent_run_%d.txt", runnumber) <<endl;
	int pmt;
	float dummy1, dummy2;
	TH1D *current = new TH1D ("current", "current", 26, -0.5, 25.5);
	for (int l = 0; l < 8; l++){
		a>>dummy1;
		a>>dummy2;	
		if (l == 0) pmt = dummy2;
		if (l > 0) current->SetBinContent(dummy1 + 1, dummy2);
		if (l > 0) current->SetBinError(dummy1 + 1, dummy2/1000);

	}
	cout << pmt <<endl;
	TCanvas *aaa = new TCanvas("aaa", "aaa", 10, 10, 800, 600);
	aaa->cd(0);
	current.SetMarkerStyle(20);
	current.SetMarkerColor(2);
	current.SetMarkerSize(2);
		current.Draw("");

	xCaption.DrawLatex(0.8, 0.03, "Cut (channels)");
	xCaption.DrawLatex(0.05, 0.93, "I, nA");
	xCaption.DrawLatex(0.45, 0.93, Form("PMT %d", pmt));

	aaa->SaveAs(Form("pmt%dCurrent.png", pmt));
}