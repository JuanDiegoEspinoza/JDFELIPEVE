

import java.io.IOException;
import java.net.URI;

import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpCoreContext;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;

import com.ibm.watson.developer_cloud.retrieve_and_rank.v1.RetrieveAndRank;

public class RR00_GetClient {

    static RetrieveAndRank service;
    private static SolrClient solrClient;

    public RR00_GetClient() {
	RetrieveAndRank service = new RetrieveAndRank();
	service.setUsernameAndPassword(DTOConversation.getUserName(), DTOConversation.getPassword());
    }

    public static HttpSolrClient getSolrClient(String uri) {
	RetrieveAndRank service = new RetrieveAndRank();
	service.setUsernameAndPassword(DTOConversation.getUserName(), DTOConversation.getPassword());
	return new HttpSolrClient(service.getSolrUrl(DTOConversation.getClusterID()), createHttpClient(uri));
    }

    private static HttpClient createHttpClient(String uri) {
	final URI scopeUri = URI.create(uri);

	final BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
	credentialsProvider.setCredentials(new AuthScope(scopeUri.getHost(), scopeUri.getPort()),
		new UsernamePasswordCredentials(DTOConversation.getUserName(), DTOConversation.getPassword()));

	final HttpClientBuilder builder = HttpClientBuilder.create().setMaxConnTotal(128).setMaxConnPerRoute(32)
		.setDefaultRequestConfig(RequestConfig.copy(RequestConfig.DEFAULT).setRedirectsEnabled(true).build())
		.setDefaultCredentialsProvider(credentialsProvider)
		.addInterceptorFirst(new PreemptiveAuthInterceptor());
	return builder.build();
    }

    public static class PreemptiveAuthInterceptor implements HttpRequestInterceptor {
	public void process(HttpRequest request, HttpContext context) throws HttpException {
	    final AuthState authState = (AuthState) context.getAttribute(HttpClientContext.TARGET_AUTH_STATE);

	    if (authState.getAuthScheme() == null) {
		final CredentialsProvider credsProvider = (CredentialsProvider) context
			.getAttribute(HttpClientContext.CREDS_PROVIDER);
		final HttpHost targetHost = (HttpHost) context.getAttribute(HttpCoreContext.HTTP_TARGET_HOST);
		final Credentials creds = credsProvider
			.getCredentials(new AuthScope(targetHost.getHostName(), targetHost.getPort()));
		if (creds == null) {
		    throw new HttpException("No creds provided for preemptive auth.");
		}
		authState.update(new BasicScheme(), creds);
	    }
	}
    }

    public void indexDocuments() throws SolrServerException, IOException {
	solrClient = getSolrClient(DTOConversation.getClusterID());

	SolrInputDocument document = new SolrInputDocument();
	document.addField("id", 1);
	document.addField("author", "brenckman,m.");
	document.addField("bibliography", "j. ae. scs. 25, 1958, 324.");
	document.addField("body",
		"experimental investigation of the aerodynamics of a wing in a slipstream .   an experimental study of a wing in a propeller slipstream was made in order to determine the spanwise distribution of the lift increase due to slipstream at different angles of attack of the wing and at different free stream to slipstream velocity ratios .  the results were intended in part as an evaluation basis for different theoretical treatments of this problem .   the comparative span loading curves, together with supporting evidence, showed that a substantial part of the lift increment produced by the slipstream was due to a /destalling/ or boundary-layer-control effect .  the integrated remaining lift increment, after subtracting this destalling lift, was found to agree well with a potential flow theory .   an empirical evaluation of the destalling effects was made for the specific configuration of the experiment .");
	document.addField("title", "experimental investigation of the aerodynamics of a wing in a slipstream .");

	System.out.println("Indexing document...");
	UpdateResponse addResponse = solrClient.add("example_collection", document);
	System.out.println(addResponse);

	// Commit the document to the index so that it will be available for
	// searching.
	solrClient.commit("example_collection");
	System.out.println("Indexed and committed document.");
    }
    
    public static void main(String args[]) throws SolrServerException, IOException{
    	RR00_GetClient cliente = new RR00_GetClient();
    	cliente.indexDocuments();
    }
}
