

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.CollectionAdminRequest;
import org.apache.solr.client.solrj.response.CollectionAdminResponse;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;

import com.ibm.watson.developer_cloud.http.ServiceCall;
import com.ibm.watson.developer_cloud.retrieve_and_rank.v1.RetrieveAndRank;
import com.ibm.watson.developer_cloud.retrieve_and_rank.v1.model.Ranker;
import com.ibm.watson.developer_cloud.retrieve_and_rank.v1.model.Rankers;
import com.ibm.watson.developer_cloud.retrieve_and_rank.v1.model.Ranking;
import com.ibm.watson.developer_cloud.retrieve_and_rank.v1.model.SolrCluster;
import com.ibm.watson.developer_cloud.retrieve_and_rank.v1.model.SolrCluster.Status;
import com.ibm.watson.developer_cloud.retrieve_and_rank.v1.model.SolrClusterOptions;
import com.ibm.watson.developer_cloud.retrieve_and_rank.v1.model.SolrClusters;



public class ComunicaRetrieve {
	public RetrieveAndRank servicio;
	public void conectar(){
	    servicio = new RetrieveAndRank();
		servicio.setUsernameAndPassword("1d22e8ca-e291-449d-b3a5-52f77f9995a1","hwd2JntYogeP");
	}
	public SolrCluster  crearCluster(String nombreCluster) throws InterruptedException{
		SolrClusterOptions opciones = new SolrClusterOptions (nombreCluster, 1);
		SolrCluster cluster = servicio.createSolrCluster(opciones).execute();
		System.out.println("SolrCluster: " + cluster);
		while (cluster.getStatus() == Status.NOT_AVAILABLE) {
		    Thread.sleep(10000); // sleep 10 seconds
		    cluster = servicio.getSolrCluster(cluster.getId()).execute();
		    System.out.println("SolrCluster status: " + cluster.getStatus());
		    System.out.println("SolrCluster ID: " + cluster.getId());
		    
		}
		return cluster;
	}
	public void obtenerClusterExistentes(String clusterID){
	//	servicio.setUsernameAndPassword(nombreUsuario, contrasenia);

		System.out.println(servicio.getSolrCluster(clusterID).execute());
	}
	public void eliminarCluster(String clusterId){
		servicio.deleteSolrCluster(clusterId);
	}
	public void publicarConfiguracion(String direccionArchivo, String clusterID, String  nombreConfiguracion){

		//servicio = new RetrieveAndRank();
	//	servicio.setUsernameAndPassword("c8a255cb-ac04-436d-b72a-039182ffee75","2dyqCspSha8K");
		File configZip = new File(direccionArchivo);
		servicio.uploadSolrClusterConfigurationZip(clusterID,nombreConfiguracion, configZip).execute();
		
	    System.out.println(servicio.getSolrClusterConfigurations(clusterID));
	}
	public void obtenerConfiguracion(String clusterID, String nombreConfiguracion){
		Path FROM = (Path) servicio.getSolrClusterConfiguration(clusterID, nombreConfiguracion).execute();
			Path TO = Paths.get(".");
			CopyOption[] options = new CopyOption[] { StandardCopyOption.REPLACE_EXISTING,
				StandardCopyOption.COPY_ATTRIBUTES };
			try {
			    Files.copy(FROM, TO, options);
			} catch (IOException e) {
			    // TODO Auto-generated catch block
			    e.printStackTrace();
			}
	}
	public void eliminarConfiguracion(String clusterID, String nombreConfiguracion){
		servicio.deleteSolrClusterConfiguration(clusterID, nombreConfiguracion).execute();
		System.out.println("eliminado");
	}

	public void crearColeccion(String clusterID,String nombreColeccion, String nombreConfiguracion) throws SolrServerException, IOException{
		SolrClient solrClient = RR00_GetClient.getSolrClient(servicio.getSolrUrl(clusterID));

		CollectionAdminRequest.Create createCollectionRequest = new CollectionAdminRequest.Create();
		createCollectionRequest.setCollectionName(nombreColeccion);
		createCollectionRequest.setConfigName(nombreConfiguracion);

		System.out.println("Creating collection...");
		CollectionAdminResponse response = createCollectionRequest.process(solrClient);
		if (!response.isSuccess()) {
		    System.out.println(response.getErrorMessages());
		    throw new IllegalStateException("Failed to create collection: " + response.getErrorMessages().toString());
		}
		System.out.println("Collection created.");
		System.out.println(response);
	    }
	public void indexarDocumento(String clusterID, String nombreColeccion){
	    SolrClient solrClient = RR00_GetClient.getSolrClient(servicio.getSolrUrl(clusterID));

		SolrInputDocument document = new SolrInputDocument();
		document.addField("id", 4);
		document.addField("author", "Kevin Matamoros");
		document.addField("bibliography", "");
		document.addField("body",
			"Se puede representar mediante un círculo o una caja con la descripción de <<interface>> antes del nombre"


				);
		document.addField("title", "Clase Abstracta");

		System.out.println("Indexing document...");
		UpdateResponse addResponse;
		try {
		    addResponse = solrClient.add(nombreColeccion, document);
		    System.out.println(addResponse);
		} catch (SolrServerException e1) {
		    // TODO Auto-generated catch block
		    e1.printStackTrace();
		} catch (IOException e1) {
		    // TODO Auto-generated catch block
		    e1.printStackTrace();
		}

		// Commit the document to the index so that it will be available for
		// searching.
		try {
		    solrClient.commit(nombreColeccion);
		} catch (SolrServerException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		} catch (IOException e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		}
		System.out.println("Indexed and committed document.");
	    }
	
	public void consultar(String nombreColeccion, String clusterID, String pregunta) throws SolrServerException, IOException{
		SolrClient solrClient = RR00_GetClient.getSolrClient(servicio.getSolrUrl(clusterID));

		SolrQuery query = new SolrQuery(pregunta);
		QueryResponse response = solrClient.query(nombreColeccion, query);
		System.out.println(response);
	    }
	
	public void crearRank(String nombreRanker, String archivo){

		File configZip = new File(archivo);
		Ranker ranker = (Ranker) servicio.createRanker(nombreRanker, configZip).execute();
		System.out.println(ranker);
	}
	public void listarRankers(){

		Rankers rankers = servicio.getRankers().execute();
		System.out.println(rankers);
	}
	public void calificarPreguntas(String datos, String RankerID){
		File configZip = new File(datos);
		Ranking ranking = servicio.rank(RankerID, configZip,20).execute();
		System.out.println(ranking);
	    }
	public void estatusRanker(String rankerID){
		Ranker ranker = servicio.getRankerStatus(rankerID).execute();
		System.out.println(ranker);
	}

	
	public static void main(String[] args) throws InterruptedException, SolrServerException, IOException {
			ComunicaRetrieve serv= new ComunicaRetrieve();
			serv.conectar();
		//	SolrCluster cluster = serv.crearCluster("tutorCognitivo");
		  //serv.obtenerClusterExistentes("sc587f7b79_e4f1_4900_84e9_59930236a33f");
		  //serv.obtenerClusterExistentes();
		
		// serv.publicarConfiguracion("C:/Users/JuanDiego/git/JDFELIPEVE/src/ejemplocongif.zip", "sc337f2d24_66a9_4d02_bbbf_a49818ba5984", "configuracionCluster");
		//  serv.obtenerConfiguracion("sc587f7b79_e4f1_4900_84e9_59930236a33f", "pruebaW");
	
		//serv.eliminarConfiguracion("sc587f7b79_e4f1_4900_84e9_59930236a33f", "pruebaW");
		//serv.crearColeccion("sc587f7b79_e4f1_4900_84e9_59930236a33f","UML", "pruebaW");
			//serv.indexarDocumento("sc587f7b79_e4f1_4900_84e9_59930236a33f","UML");
		//serv.consultar("POO","sc587f7b79_e4f1_4900_84e9_59930236a33f", "que es uml"); 
	//serv.crearRank("rankerNuevo", "C:/Users/JuanDiego/git/JDFELIPEVE/src/pruebaRanker.txt");
		//	serv.listarRankers();
		//	serv.estatusRanker("c852bax18-rank-2026");
		//serv.servicio.deleteRanker("PRUEBAfELIPE");
		serv.calificarPreguntas("C:/Users/JuanDiego/git/JDFELIPEVE/src/pruebaInternet2.csv", "c852bax18-rank-2026");
			
			
			
	    }
		
		//service.uploadSolrClusterConfigurationDirectory(solrClusterID, nameCluster, "qwerty.pdf");
		
	}
