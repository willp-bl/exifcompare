ExifCompare 0.0.1
=================

This code has two main classes:
* ExifCompare: uses Apache Tika to compare metadata from two files
* ExifCompareV2: uses Apache Commons-Imaging (1.0-SNAPSHOT) to compare metadata from two files and insert new metadata in to a TIFF

As there are no current releases of Apache Commons-Imaging in Maven Central, you need to checkout, package and install a snapshot into your Maven repo.
http://commons.apache.org/proper/commons-imaging/source-repository.html

Note that the ExifCompareV2 output images need to be checked and verified as the code currently outputs larger files than expected.

WARNING
=======
As it stands this is prototype code and likely to damage files, so be careful! 

