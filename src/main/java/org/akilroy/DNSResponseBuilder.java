package org.akilroy;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * @author Andrew Kilroy
 */
public class DNSResponseBuilder
{
    private static final int OPS_QR_RESPONSE = 0x80;
    private static final int OPS_DR_RESPONSE = 0x01;

    private final int headerID;
    private final ByteBuf questions = Unpooled.buffer(128);
    private final ByteBuf answers = Unpooled.buffer();
    private int questionCount = 0;
    private int answerCount = 0;
    private int rcode = 0;

    public DNSResponseBuilder(DNSHeader queryHeader)
    {
        headerID = queryHeader.getID();
    }

    public void appendAnswer(ByteBuf namebuf, int type, int theClass, int ttl, byte[] rdata)
    {
        answers.writeBytes(namebuf);
        answers.writeShort(type);   // 2
        answers.writeShort(theClass); // 2
        answers.writeInt(ttl);           // 4
        answers.writeShort(rdata.length);           // 2
        answers.writeBytes(rdata); // 4
        answerCount++;
    }

    public void appendQuestion(ByteBuf questionBuf)
    {
        questions.writeBytes(questionBuf);
        questionCount++;
    }

    public void writeResponse(ByteBuf outstream)
    {
        outstream
            .writeShort(headerID)
            .writeByte(OPS_DR_RESPONSE | OPS_QR_RESPONSE)
            .writeByte(rcode) // TODO do bitwise OR with RA bit
            .writeShort(questionCount)
            .writeShort(answerCount)
            .writeShort(0) // number of name server records
            .writeShort(0);// number of additional records
        outstream
            .writeBytes(questions.slice())
            .writeBytes(answers.slice());

    }

    public void setRCode(int code)
    {
        rcode = code;
    }
}
