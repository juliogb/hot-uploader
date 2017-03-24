/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.hotmart.hot.uploader.rest;

import com.hotmart.hot.uploader.model.ChunkMetaInfo;
import com.hotmart.hot.uploader.model.FileMetaInfo;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;
import org.jboss.arquillian.extension.rest.client.ArquillianResteasyResource;
import org.jboss.arquillian.junit.InSequence;
import org.junit.runner.RunWith;

/**
 *
 * @author julio
 */
@RunWith(Arquillian.class)
public class UploadRESTTest {
    
    private static final String MD5 = "0d0c77f474c80a5b7387ff91da7cb554";
    private static final String USER = "julio";
    private static final File file = new File("./src/test/resources/files/Simbolo-Hotmart.png");
    private static final File part1 = new File("./src/test/resources/files/part1");
    private static final File part2 = new File("./src/test/resources/files/part2");
    private Long fileSize;
    private Long part1Size;
    private Long part2Size;
    
    @Deployment()
    public static WebArchive createDeployment() throws FileNotFoundException {
        File[] libs = Maven.resolver().loadPomFromFile("pom.xml")
                .importRuntimeDependencies()
                .resolve("org.assertj:assertj-core")
                .withTransitivity().asFile();
        
        WebArchive war = ShrinkWrap.create(WebArchive.class)
                .addPackages(true, "com.hotmart.hot.uploader")
                .addAsLibraries(libs);
        return war;
    }
    
    public UploadRESTTest() {
        this.fileSize = FileUtils.sizeOf(file);
        this.part1Size = FileUtils.sizeOf(part1);
        this.part2Size = FileUtils.sizeOf(part2);
    }
    
    /**
     * Test of upload method, of class UploadREST.
     */
    @Test
    @InSequence(1)
    public void testUpload(@ArquillianResteasyResource UploadREST uploadREST) throws IOException {
        
        ChunkMetaInfo chunk = new ChunkMetaInfo();
        chunk.setFileMD5(MD5);
        chunk.setContentRange("bytes 0-"+part1Size+"/"+fileSize);
        chunk.setFileName(part1.getName());
        chunk.setUser(USER);
        FileUtils.getUserDirectoryPath();
        
        //part1
        Response response = uploadREST.upload(chunk, FileUtils.openInputStream(part1));
        assertThat(response.getStatus()).isEqualTo(204);
        //part2
        chunk = new ChunkMetaInfo();
        chunk.setFileMD5(MD5);
        chunk.setUser(USER);
        chunk.setContentRange("bytes "+part1Size+"-"+(part1Size+part2Size)+"/"+fileSize);
        response = uploadREST.upload(chunk, FileUtils.openInputStream(part2));
        assertThat(response.getStatus()).isEqualTo(200);
    }
    
    /**
     * Test of findOne method, of class UploadREST.
     */
    @Test
    @InSequence(2)
    public void testFindOne(@ArquillianResteasyResource UploadREST uploadREST) {
        Response response = uploadREST.findOne(MD5);
        FileMetaInfo fileMetaInfo = (FileMetaInfo) response.getEntity();
        
        assertThat(fileMetaInfo).isNotNull();
        assertThat(fileMetaInfo.getMd5()).isEqualTo(MD5);
        assertThat(fileMetaInfo.getLength()).isEqualTo(FileUtils.sizeOf(file));
        
    }
    
    /**
     * Test of download method, of class UploadREST.
     */
    @Test
    @InSequence(3)
    // @GET @Path("v1/file/download/"+MD5)
    public void testDownload(@ArquillianResteasyResource UploadREST uploadREST) throws Exception {
        
        Response response = uploadREST.download(MD5);
        StreamingOutput fileDownload = (StreamingOutput) response.getEntity();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        fileDownload.write(baos);
        FileUtils.writeByteArrayToFile(new File("./src/test/resources/files/download"), baos.toByteArray());
        assertThat(DigestUtils.md5Hex(baos.toByteArray())).isEqualTo(MD5);
    }
    
    /**
     * Test of findAll method, of class UploadREST.
     */
    @Test
    @InSequence(4)
    public void testFindAll(@ArquillianResteasyResource() UploadREST uploadREST) {
        
        Response response = uploadREST.findAll();
        Collection<FileMetaInfo> files = (Collection<FileMetaInfo>) response.getEntity();
        
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(files).hasSize(1).extracting(FileMetaInfo::getMd5).contains(MD5);
    }
    
    @Test
    public void testUploadValidation(@ArquillianResteasyResource UploadREST uploadREST) {
        try{
            Response response = uploadREST.upload(new ChunkMetaInfo(), null);
        }catch(ConstraintViolationException e){
            assertThat(e.getConstraintViolations()).hasSize(3)
                    .extracting(ConstraintViolation::getMessageTemplate)
                    .containsOnly("{javax.validation.constraints.NotNull.message}");
        }
    }
    
}
