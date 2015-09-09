package ca.mcgill.mcb.pcingola.snpSift.testCases;

import java.io.File;

import org.junit.Assert;

import ca.mcgill.mcb.pcingola.fileIterator.VcfFileIterator;
import ca.mcgill.mcb.pcingola.interval.Marker;
import ca.mcgill.mcb.pcingola.interval.Markers;
import ca.mcgill.mcb.pcingola.snpSift.annotate.IntervalFile;
import ca.mcgill.mcb.pcingola.snpSift.annotate.MarkerFile;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;
import junit.framework.TestCase;

/**
 * Try test cases in this class before adding them to long test cases
 *
 * @author pcingola
 */
public class TestCasesZzz extends TestCase {

	public static boolean debug = false;
	public static boolean verbose = false || debug;

	/**
	 * Index a VCF file and query all entries
	 */
	public void test_01() {
		Gpr.debug("Test");
		String dbFileName = "./test/db_test_index_01.vcf";

		// Make sure index file is deleted
		String indexFileName = dbFileName + "." + IntervalFile.INDEX_EXT;
		(new File(indexFileName)).delete();

		// Index VCF file
		IntervalFile vcfIndex = new IntervalFile(dbFileName);
		vcfIndex.setVerbose(verbose);
		vcfIndex.index();
		vcfIndex.open();

		// Check that all entries can be found & retrieved
		if (verbose) Gpr.debug("Checking");
		VcfFileIterator vcf = new VcfFileIterator(dbFileName);
		for (VcfEntry ve : vcf) {
			if (verbose) System.out.println(ve.toStr());

			// Query database
			Markers results = vcfIndex.query(ve);

			// We should find at least one result
			Assert.assertTrue("No results found for entry:\n\t" + ve, results.size() > 0);

			// Check each result
			for (Marker res : results) {
				MarkerFile resmf = (MarkerFile) res;
				VcfEntry veIdx = vcfIndex.read(resmf);
				if (verbose) System.out.println("\t" + res + "\t" + veIdx);

				// Check that result does intersect query
				Assert.assertTrue("Selected interval does not intersect marker form file!" //
						+ "\n\tVcfEntry: " + ve //
						+ "\n\tResult: " + res //
						+ "\n\tVcfEntry from result:" + veIdx//
						, ve.intersects(veIdx) //
				);
			}
		}

		vcfIndex.close();
	}

	/**
	 * Index a VCF file and query all entries
	 */
	public void test_02() {
		Gpr.debug("Test");
		String dbFileName = "./test/db_test_index_02.vcf";

		// Index VCF file
		String indexFileName = dbFileName + "." + IntervalFile.INDEX_EXT;
		(new File(indexFileName)).delete();

		// Create index file
		IntervalFile vcfIndex = new IntervalFile(dbFileName);
		vcfIndex.setVerbose(verbose);
		vcfIndex.index();

		// Make sure index file was created
		Assert.assertTrue("Index file '" + indexFileName + "' does not exist", Gpr.exists(indexFileName));

		// Restart so we force to read from index file
		vcfIndex = new IntervalFile(dbFileName);
		vcfIndex.setVerbose(verbose);
		vcfIndex.index();
		vcfIndex.open();

		// Check that all entries can be found & retrieved
		if (verbose) Gpr.debug("Checking");
		VcfFileIterator vcf = new VcfFileIterator(dbFileName);
		for (VcfEntry ve : vcf) {
			if (verbose) System.out.println(ve.toStr());

			// Query database
			Markers results = vcfIndex.query(ve);

			// We should find at least one result
			Assert.assertTrue("No results found for entry:\n\t" + ve, results.size() > 0);

			// Check each result
			for (Marker res : results) {
				MarkerFile resmf = (MarkerFile) res;
				VcfEntry veIdx = vcfIndex.read(resmf);
				if (verbose) System.out.println("\t" + res + "\t" + veIdx);

				// Check that result does intersect query
				Assert.assertTrue("Selected interval does not intersect marker form file!" //
						+ "\n\tVcfEntry: " + ve //
						+ "\n\tResult: " + res //
						+ "\n\tVcfEntry from result:" + veIdx//
						, ve.intersects(veIdx) //
				);
			}
		}

		vcfIndex.close();
	}

}