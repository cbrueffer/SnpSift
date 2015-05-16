package ca.mcgill.mcb.pcingola.snpSift.annotate;

import ca.mcgill.mcb.pcingola.fileIterator.VcfFileIterator;
import ca.mcgill.mcb.pcingola.util.Gpr;
import ca.mcgill.mcb.pcingola.vcf.VcfEntry;

/**
 * Use a bgzip-compressed, tabix indexed VCF file as a database for annotations
 *
 * @author pcingola
 */
public class DbVcfTabix extends DbVcf {

	public static final int MIN_SEEK = 100;

	public DbVcfTabix(String dbFileName) {
		super(dbFileName);
	}

	/**
	 * Seek to a new position. Make sure we advance at least one entry
	 */
	void dbSeek(VcfEntry vcfEntry) {

		// Current coordinates
		String chr = "";
		int pos = -1;

		if (latestVcfDb != null) {
			pos = latestVcfDb.getStart();
			chr = latestVcfDb.getChromosomeName();
			if (debug) Gpr.debug("Position seek:\t" + chr + ":" + pos + "\t->\t" + vcfEntry.getChromosomeName() + ":" + vcfEntry.getStart());
		}

		// Seek
		vcfDbFile.seek(vcfEntry.getChromosomeName(), vcfEntry.getStart());

		// Make sure we actually advance at least one entry
		do {
			latestVcfDb = vcfDbFile.next();
			if (debug) Gpr.debug("After seek: " + latestVcfDb.getChromosomeName() + ":" + latestVcfDb.getStart() + "\t" + chr + ":" + pos);
			if (latestVcfDb == null) break;
		} while (latestVcfDb != null && latestVcfDb.getChromosomeName().equals(chr) && latestVcfDb.getStart() <= pos);
	}

	/**
	 * Open database annotation file
	 */
	@Override
	public void open() {
		// Open database
		vcfDbFile = new VcfFileIterator(dbFileName);
		vcfDbFile.setDebug(debug);
		if (!vcfDbFile.isTabix()) throw new RuntimeException("Could not open VCF file as TABIX-indexed: '" + dbFileName + "'");
		latestVcfDb = vcfDbFile.next(); // Read first VCf entry from DB file (this also forces to read headers)
		add(latestVcfDb);
	}

	/**
	 * Find a matching db entry for a vcf entry
	 */
	@Override
	public void readDb(VcfEntry vcfEntry) {
		//---
		// Find db entry
		//---
		if (debug) System.err.println("Looking for " + vcfEntry.getChromosomeName() + ":" + vcfEntry.getStart() + ". Current DB: " + (latestVcfDb == null ? "null" : latestVcfDb.getChromosomeName() + ":" + latestVcfDb.getStart()));
		while (true) {

			if (latestVcfDb == null) {
				// Null entry, try getting next entry
				latestVcfDb = vcfDbFile.next(); // Read next DB entry

				// Still null? May be we run out of DB entries for this chromosome
				if (latestVcfDb == null) {
					// Is vcfEntry still in 'latestChromo'? Then we have no DbEntry, return null
					if (checkChromo(vcfEntry)) {
						// End of 'latestChromo' section in database?
						return;
					}

					dbSeek(vcfEntry); // VCfEntry is in another chromosome? seek to 'new' chromosome

					// Still null? well it looks like we don't have any dbEntry for this chromosome
					if (latestVcfDb == null) {
						updateChromo(vcfEntry); // Make sure we don't try seeking again
						return;
					}
				}
			}

			if (debug) Gpr.debug("Current Db Entry:" + latestVcfDb.getChromosomeName() + ":" + latestVcfDb.getStart() + "\tLooking for: " + vcfEntry.getChromosomeName() + ":" + vcfEntry.getStart());

			// Find entry
			if (latestVcfDb.getChromosomeName().equals(vcfEntry.getChromosomeName())) {
				// Same chromosome

				// Same position? => Found
				if (vcfEntry.getStart() == latestVcfDb.getStart()) {
					// Found db entry! Break loop and proceed with annotations
					if (debug) Gpr.debug("Found Db Entry:" + latestVcfDb.getChromosomeName() + ":" + latestVcfDb.getStart());
					add(latestVcfDb);
					latestVcfDb = vcfDbFile.next();
				} else if (vcfEntry.getStart() < latestVcfDb.getStart()) {
					// Same chromosome, but positioned after => No db entry found
					if (debug) Gpr.debug("No db entry found:\t" + vcfEntry.getChromosomeName() + ":" + vcfEntry.getStart());
					return;
				} else if ((vcfEntry.getStart() - latestVcfDb.getStart()) > MIN_SEEK) {
					// Is it far enough? Don't iterate, seek
					clear();
					dbSeek(vcfEntry);
				} else {
					// Just read next entry to get closer
					latestVcfDb = vcfDbFile.next();
					clear();
				}
			} else if (!latestVcfDb.getChromosomeName().equals(vcfEntry.getChromosomeName())) {
				// Different chromosome? => seek to chromosome
				if (debug) Gpr.debug("Chromosome seek:\t" + latestVcfDb.getChromosomeName() + ":" + latestVcfDb.getStart() + "\t->\t" + vcfEntry.getChromosomeName() + ":" + vcfEntry.getStart());

				// seek to new position. If chromosome not found, return null
				if (!vcfDbFile.seek(vcfEntry.getChromosomeName(), vcfEntry.getStart())) return;

				clear();
				latestVcfDb = vcfDbFile.next();
			}

			if (latestVcfDb != null) updateChromo(latestVcfDb);
		}
	}

}