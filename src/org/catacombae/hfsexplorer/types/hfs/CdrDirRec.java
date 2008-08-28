package org.catacombae.hfsexplorer.types.hfs;

import java.io.PrintStream;
import org.catacombae.hfsexplorer.Util;

/** This class was generated by CStructToJavaClass. */
public class CdrDirRec {
    /*
     * struct CdrDirRec
     * size: 36 bytes
     * description: 
     * 
     * BP  Size  Type       Identifier   Description                                 
     * ------------------------------------------------------------------------------
     * 0   1     SInt8      cdrType      record type (SignedByte)                    
     * 1   1     SInt8      cdrResrv2    reserved (SignedByte)                       
     * 2   2     SInt16     dirFlags     directory flags (Integer)                   
     * 4   2     SInt16     dirVal       directory valence (Integer)                 
     * 6   4     SInt32     dirDirID     directory ID (LongInt)                      
     * 10  4     SInt32     dirCrDat     date and time of creation (LongInt)         
     * 14  4     SInt32     dirMdDat     date and time of last modification (LongInt)
     * 18  4     SInt32     dirBkDat     date and time of last backup (LongInt)      
     * 22  16    DInfo      dirUsrInfo   Finder information (DInfo)                  
     * 21  16    DXInfo     dirFndrInfo  additional Finder information (DXInfo)      
     * 20  4*4   SInt32[4]  dirResrv     reserved (ARRAY[1..4] OF LongInt)           
     */
    
    public static final int STRUCTSIZE = 36;
    
    private final byte[] cdrType = new byte[1];
    private final byte[] cdrResrv2 = new byte[1];
    private final byte[] dirFlags = new byte[2];
    private final byte[] dirVal = new byte[2];
    private final byte[] dirDirID = new byte[4];
    private final byte[] dirCrDat = new byte[4];
    private final byte[] dirMdDat = new byte[4];
    private final byte[] dirBkDat = new byte[4];
    private final DInfo dirUsrInfo;
    private final DXInfo dirFndrInfo;
    private final byte[] dirResrv = new byte[4*4];
    
    public CdrDirRec(byte[] data, int offset) {
	System.arraycopy(data, offset+0, cdrType, 0, 1);
	System.arraycopy(data, offset+1, cdrResrv2, 0, 1);
	System.arraycopy(data, offset+2, dirFlags, 0, 2);
	System.arraycopy(data, offset+4, dirVal, 0, 2);
	System.arraycopy(data, offset+6, dirDirID, 0, 4);
	System.arraycopy(data, offset+10, dirCrDat, 0, 4);
	System.arraycopy(data, offset+14, dirMdDat, 0, 4);
	System.arraycopy(data, offset+18, dirBkDat, 0, 4);
	dirUsrInfo = new DInfo(data, offset+22);
	dirFndrInfo = new DXInfo(data, offset+21);
	System.arraycopy(data, offset+20, dirResrv, 0, 4*4);
    }
    
    public static int length() { return STRUCTSIZE; }
    
    /** record type (SignedByte) */
    public byte getCdrType() { return Util.readByteBE(cdrType); }
    /** reserved (SignedByte) */
    public byte getCdrResrv2() { return Util.readByteBE(cdrResrv2); }
    /** directory flags (Integer) */
    public short getDirFlags() { return Util.readShortBE(dirFlags); }
    /** directory valence (Integer) */
    public short getDirVal() { return Util.readShortBE(dirVal); }
    /** directory ID (LongInt) */
    public int getDirDirID() { return Util.readIntBE(dirDirID); }
    /** date and time of creation (LongInt) */
    public int getDirCrDat() { return Util.readIntBE(dirCrDat); }
    /** date and time of last modification (LongInt) */
    public int getDirMdDat() { return Util.readIntBE(dirMdDat); }
    /** date and time of last backup (LongInt) */
    public int getDirBkDat() { return Util.readIntBE(dirBkDat); }
    /** Finder information (DInfo) */
    public DInfo getDirUsrInfo() { return dirUsrInfo; }
    /** additional Finder information (DXInfo) */
    public DXInfo getDirFndrInfo() { return dirFndrInfo; }
    /** reserved (ARRAY[1..4] OF LongInt) */
    public int[] getDirResrv() { return Util.readIntArrayBE(dirResrv); }
    
    public void printFields(PrintStream ps, String prefix) {
	ps.println(prefix + " cdrType: " + getCdrType());
	ps.println(prefix + " cdrResrv2: " + getCdrResrv2());
	ps.println(prefix + " dirFlags: " + getDirFlags());
	ps.println(prefix + " dirVal: " + getDirVal());
	ps.println(prefix + " dirDirID: " + getDirDirID());
	ps.println(prefix + " dirCrDat: " + getDirCrDat());
	ps.println(prefix + " dirMdDat: " + getDirMdDat());
	ps.println(prefix + " dirBkDat: " + getDirBkDat());
	ps.println(prefix + " dirUsrInfo: ");
	getDirUsrInfo().print(ps, prefix+"  ");
	ps.println(prefix + " dirFndrInfo: ");
	getDirFndrInfo().print(ps, prefix+"  ");
	ps.println(prefix + " dirResrv: " + getDirResrv());
    }
    
    public void print(PrintStream ps, String prefix) {
	ps.println(prefix + "CdrDirRec:");
	printFields(ps, prefix);
    }
    
    public byte[] getBytes() {
	byte[] result = new byte[STRUCTSIZE];
	byte[] tempData;
	int offset = 0;
	System.arraycopy(cdrType, 0, result, offset, cdrType.length); offset += cdrType.length;
	System.arraycopy(cdrResrv2, 0, result, offset, cdrResrv2.length); offset += cdrResrv2.length;
	System.arraycopy(dirFlags, 0, result, offset, dirFlags.length); offset += dirFlags.length;
	System.arraycopy(dirVal, 0, result, offset, dirVal.length); offset += dirVal.length;
	System.arraycopy(dirDirID, 0, result, offset, dirDirID.length); offset += dirDirID.length;
	System.arraycopy(dirCrDat, 0, result, offset, dirCrDat.length); offset += dirCrDat.length;
	System.arraycopy(dirMdDat, 0, result, offset, dirMdDat.length); offset += dirMdDat.length;
	System.arraycopy(dirBkDat, 0, result, offset, dirBkDat.length); offset += dirBkDat.length;
	tempData = dirUsrInfo.getBytes();
	System.arraycopy(tempData, 0, result, offset, tempData.length); offset += tempData.length;
	tempData = dirFndrInfo.getBytes();
	System.arraycopy(tempData, 0, result, offset, tempData.length); offset += tempData.length;
	System.arraycopy(dirResrv, 0, result, offset, dirResrv.length); offset += dirResrv.length;
	return result;
    }
}