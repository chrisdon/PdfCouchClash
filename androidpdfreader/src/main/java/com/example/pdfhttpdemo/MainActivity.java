package com.example.pdfhttpdemo;

import android.app.Activity;
import android.os.Bundle;

import com.radaee.pdf.Document;
import com.radaee.pdf.Global;
import com.radaee.util.PDFHttpStream;

public class MainActivity extends Activity
{
    private ReaderController m_vPDF = null;
    private Document m_doc = new Document();
    private PDFHttpStream m_stream = new PDFHttpStream();

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        Global.Init( this );
        m_vPDF = new ReaderController(this);
        m_doc.Close();
        m_stream.open("http://www.androidpdf.mobi/docs/MRBrochoure.pdf");
        //m_stream.open("http://www.radaee.com/files/test.pdf");
        int ret = m_doc.OpenStream(m_stream, null);

        switch( ret )
        {
            case -1://need input password
                finish();
                break;
            case -2://unknown encryption
                finish();
                break;
            case -3://damaged or invalid format
                finish();
                break;
            case -10://access denied or invalid file path
                finish();
                break;
            case 0://succeeded, and continue
                break;
            default://unknown error
                finish();
                break;
        }

        if( ret == 0 )
        {
	        m_vPDF.open(m_doc);
	        setContentView( m_vPDF );
        }
	}

	@Override
    public void onDestroy()
    {
        if( m_vPDF != null )
        {
            m_vPDF.close();
            m_vPDF = null;
        }
        if( m_doc != null )
        {
        	m_doc.Close();
        	m_doc = null;
        }
        if( m_stream != null )
        {
        	m_stream.close();
        	m_stream = null;
        }
        Global.RemoveTmp();
        super.onDestroy();
    }

}
