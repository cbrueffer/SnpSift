package org.snpsift.testCases;

import java.util.ArrayList;
import java.util.List;

import org.snpeff.vcf.VcfEntry;
import org.snpsift.SnpSift;
import org.snpsift.SnpSiftCmdFilterChrPos;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * Test cases for SnpSift filterChrPos
 *
 * @author pcingola
 */
public class TestCasesFilterChrPos extends TestCase {

	public static boolean debug = false;
	public static boolean verbose = false || debug;

	protected String[] defaultExtraArgs = null;

	protected String[] argsList(String chrPosFileName, String vcfFileName, String[] extraArgs) {
		ArrayList<String> argsList = new ArrayList<>();

		argsList.add("filterChrPos");

		if (defaultExtraArgs != null) {
			for (String arg : defaultExtraArgs)
				argsList.add(arg);
		}

		if (extraArgs != null) {
			for (String arg : extraArgs)
				argsList.add(arg);
		}

		argsList.add(chrPosFileName);
		argsList.add(vcfFileName);
		return argsList.toArray(new String[0]);
	}

	public void filter(String dbFileName, String fileName, int expectedCount) {
		filter(dbFileName, fileName, null, expectedCount);
	}

	/**
	 * Filter
	 */
	public List<VcfEntry> filter(String chrPosFileName, String vcfFileName, String[] extraArgs, int expectedCount) {
		System.out.println("Annotate: " + chrPosFileName + "\t" + vcfFileName);

		// Create command line
		String args[] = argsList(chrPosFileName, vcfFileName, extraArgs);

		// Iterate over VCF entries
		SnpSift snpSift = new SnpSift(args);
		SnpSiftCmdFilterChrPos snpSiftCmd = (SnpSiftCmdFilterChrPos) snpSift.cmd();
		snpSiftCmd.setDebug(debug);
		snpSiftCmd.setVerbose(verbose);
		snpSiftCmd.setSuppressOutput(!verbose);
		List<VcfEntry> results = snpSiftCmd.run(true);

		// Check
		Assert.assertTrue(results != null);
		Assert.assertTrue(results.size() > 0);
		Assert.assertEquals(expectedCount, results.size());
		return results;
	}

	public void test_01() {
		String chrPosFileName = "./test/filterChrPos_01.txt";
		String fileName = "./test/filterChrPos_01.vcf";
		int expectedCount = 10;
		filter(chrPosFileName, fileName, expectedCount);
	}

	public void test_02() {
		String chrPosFileName = "./test/filterChrPos_02.txt";
		String fileName = "./test/filterChrPos_02.vcf";
		int expectedCount = 10;
		filter(chrPosFileName, fileName, expectedCount);
	}

	public void test_03() {
		String chrPosFileName = "./test/filterChrPos_03.txt";
		String fileName = "./test/filterChrPos_03.vcf";
		int expectedCount = 10;
		filter(chrPosFileName, fileName, expectedCount);
	}

}
