int istart = args[0] as int;
//int iend = 5386;
int nfiles = args[1] as int;

for( int ii = 0; ii < nfiles; ii++ ){
	int irunnum = istart + ii;	
	String srunnum = irunnum as String;
	run(new File("pmtAnalysis.groovy"), srunnum);
}
int iend = istart + nfiles - 1;
println(" Done analyzing run numbers : " + istart + " - " + iend);
