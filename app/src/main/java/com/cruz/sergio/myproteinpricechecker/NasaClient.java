package com.cruz.sergio.myproteinpricechecker;

/**
 * Created by Sergio on 27/01/2018.
 */

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.CertificatePinner;
import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.Buffer;

public final class NasaClient {
   private final OkHttpClient client;

   public NasaClient() throws GeneralSecurityException {
      // Configure cipher suites to demonstrate how to customize which cipher suites will be used for
      // an OkHttp request. In order to be selected a cipher suite must be included in both OkHttp's
      // connection spec and in the SSLSocket's enabled cipher suites array. Most applications should
      // not customize the cipher suites list.
      List<CipherSuite> customCipherSuites = Arrays.asList(
          //CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
          //CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,
          //CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256,
          //CipherSuite.TLS_DHE_RSA_WITH_AES_256_GCM_SHA384,
          //CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256,
          //CipherSuite.TLS_RSA_WITH_AES_128_CBC_SHA256,
          CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384,
          CipherSuite.TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA);
      final ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
          .cipherSuites(customCipherSuites.toArray(new CipherSuite[0]))
          .build();

      boolean useCustomTrustManager = false;
      X509TrustManager trustManager = useCustomTrustManager
          ? trustManagerForCertificates(trustedCertificatesInputStream())
          : defaultTrustManager();
      SSLSocketFactory sslSocketFactory = defaultSslSocketFactory(trustManager);
      SSLSocketFactory customSslSocketFactory = new DelegatingSSLSocketFactory(sslSocketFactory) {
         @Override
         protected SSLSocket configureSocket(SSLSocket socket) throws IOException {
            socket.setEnabledCipherSuites(javaNames(spec.cipherSuites()));
            return socket;
         }
      };

      client = new OkHttpClient.Builder()
          //.connectionSpecs(Collections.singletonList(spec))
          .sslSocketFactory(sslSocketFactory, trustManager)
          .build();
   }

   public static void main(String url) throws Exception {
      new NasaClient().run(url);
   }

   /**
    * Returns the VM's default SSL socket factory, using {@code trustManager} for trusted root
    * certificates.
    */
   private SSLSocketFactory defaultSslSocketFactory(X509TrustManager trustManager)
       throws NoSuchAlgorithmException, KeyManagementException {
      //Security.getProviders();
      SSLContext sslContext = SSLContext.getInstance("TLS");
      sslContext.init(null, new TrustManager[]{trustManager}, null);
      return sslContext.getSocketFactory();
   }

   /**
    * Returns a trust manager that trusts the VM's default certificate authorities.
    */
   private X509TrustManager defaultTrustManager() throws GeneralSecurityException {
      TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
          TrustManagerFactory.getDefaultAlgorithm());
      trustManagerFactory.init((KeyStore) null);
      TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
      if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
         throw new IllegalStateException("Unexpected default trust managers:"
             + Arrays.toString(trustManagers));
      }
      return (X509TrustManager) trustManagers[0];
   }

   private String[] javaNames(List<CipherSuite> cipherSuites) {
      String[] result = new String[cipherSuites.size()];
      for (int i = 0; i < result.length; i++) {
         result[i] = cipherSuites.get(i).javaName();
      }
      return result;
   }

   /**
    * Returns a trust manager that trusts {@code certificates} and none other. HTTPS services whose
    * certificates have not been signed by these certificates will fail with a {@code
    * SSLHandshakeException}.
    * <p>
    * <p>This can be used to replace the host platform's built-in trusted certificates with a custom
    * set. This is useful in development where certificate authority-trusted certificates aren't
    * available. Or in production, to avoid reliance on third-party certificate authorities.
    * <p>
    * <p>See also {@link CertificatePinner}, which can limit trusted certificates while still using
    * the host platform's built-in trust store.
    * <p>
    * <h3>Warning: Customizing Trusted Certificates is Dangerous!</h3>
    * <p>
    * <p>Relying on your own trusted certificates limits your server team's ability to update their
    * TLS certificates. By installing a specific set of trusted certificates, you take on additional
    * operational complexity and limit your ability to migrate between certificate authorities. Do
    * not use custom trusted certificates in production without the blessing of your server's TLS
    * administrator.
    */
   private X509TrustManager trustManagerForCertificates(InputStream in)
       throws GeneralSecurityException {
      CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
      Collection<? extends Certificate> certificates = certificateFactory.generateCertificates(in);
      if (certificates.isEmpty()) {
         throw new IllegalArgumentException("expected non-empty set of trusted certificates");
      }

      // Put the certificates a key store.
      char[] password = "password".toCharArray(); // Any password will work.
      KeyStore keyStore = newEmptyKeyStore(password);
      int index = 0;
      for (Certificate certificate : certificates) {
         String certificateAlias = Integer.toString(index++);
         keyStore.setCertificateEntry(certificateAlias, certificate);
      }

      // Use it to build an X509 trust manager.
      KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
          KeyManagerFactory.getDefaultAlgorithm());
      keyManagerFactory.init(keyStore, password);
      TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
          TrustManagerFactory.getDefaultAlgorithm());
      trustManagerFactory.init(keyStore);
      TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
      if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
         throw new IllegalStateException("Unexpected default trust managers:"
             + Arrays.toString(trustManagers));
      }
      return (X509TrustManager) trustManagers[0];
   }

   private InputStream trustedCertificatesInputStream() {
      String idenTrustCommercialRootCa = ""
          + "-----BEGIN CERTIFICATE-----\n"
          + "MIIDSjCCAjKgAwIBAgIQRK+wgNajJ7qJMDmGLvhAazANBgkqhkiG9w0BAQUFADA/\n"
          + "MSQwIgYDVQQKExtEaWdpdGFsIFNpZ25hdHVyZSBUcnVzdCBDby4xFzAVBgNVBAMT\n"
          + "DkRTVCBSb290IENBIFgzMB4XDTAwMDkzMDIxMTIxOVoXDTIxMDkzMDE0MDExNVow\n"
          + "PzEkMCIGA1UEChMbRGlnaXRhbCBTaWduYXR1cmUgVHJ1c3QgQ28uMRcwFQYDVQQD\n"
          + "Ew5EU1QgUm9vdCBDQSBYMzCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEB\n"
          + "AN+v6ZdQCINXtMxiZfaQguzH0yxrMMpb7NnDfcdAwRgUi+DoM3ZJKuM/IUmTrE4O\n"
          + "rz5Iy2Xu/NMhD2XSKtkyj4zl93ewEnu1lcCJo6m67XMuegwGMoOifooUMM0RoOEq\n"
          + "OLl5CjH9UL2AZd+3UWODyOKIYepLYYHsUmu5ouJLGiifSKOeDNoJjj4XLh7dIN9b\n"
          + "xiqKqy69cK3FCxolkHRyxXtqqzTWMIn/5WgTe1QLyNau7Fqckh49ZLOMxt+/yUFw\n"
          + "7BZy1SbsOFU5Q9D8/RhcQPGX69Wam40dutolucbY38EVAjqr2m7xPi71XAicPNaD\n"
          + "aeQQmxkqtilX4+U9m5/wAl0CAwEAAaNCMEAwDwYDVR0TAQH/BAUwAwEB/zAOBgNV\n"
          + "HQ8BAf8EBAMCAQYwHQYDVR0OBBYEFMSnsaR7LHH62+FLkHX/xBVghYkQMA0GCSqG\n"
          + "SIb3DQEBBQUAA4IBAQCjGiybFwBcqR7uKGY3Or+Dxz9LwwmglSBd49lZRNI+DT69\n"
          + "ikugdB/OEIKcdBodfpga3csTS7MgROSR6cz8faXbauX+5v3gTt23ADq1cEmv8uXr\n"
          + "AvHRAosZy5Q6XkjEGB5YGV8eAlrwDPGxrancWYaLbumR9YbK+rlmM6pZW87ipxZz\n"
          + "R8srzJmwN0jP41ZL9c8PDHIyh8bwRLtTcm1D9SZImlJnt1ir/md2cXjbDaJWFBM5\n"
          + "JDGFoqgCWjBH4d1QB7wCCZAA62RjYJsWvIjJEubSfZGL+T0yjWW06XyxV3bqxbYo\n"
          + "Ob8VZRzI9neWagqNdwvYkQsEjgfbKbYK7p2CNTUQ\n"
          + "-----END CERTIFICATE-----\n";
      return new Buffer()
          .writeUtf8(idenTrustCommercialRootCa)
          .inputStream();
   }

   private KeyStore newEmptyKeyStore(char[] password) throws GeneralSecurityException {
      try {
         KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
         InputStream in = null; // By convention, 'null' creates an empty key store.
         keyStore.load(in, password);
         return keyStore;
      } catch (IOException e) {
         throw new AssertionError(e);
      }
   }

   public void run(String url) {
      Request request = new Request.Builder()
          .url(url)
          .build();

      try (Response response = client.newCall(request).execute()) {
         //if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

         Log.i("Sergio>", this + " run\n= " + response.handshake().cipherSuite());
         Log.i("Sergio>", this + " run\n= " + response.body().string());
      } catch (IOException e) {
         e.printStackTrace();
      }
   }

   /**
    * An SSL socket factory that forwards all calls to a delegate. Override {@link #configureSocket}
    * to customize a created socket before it is returned.
    */
   static class DelegatingSSLSocketFactory extends SSLSocketFactory {
      static String TLS_v1_1 = "TLSv1.1";
      static String TLS_v1_2 = "TLSv1.2";
      protected final SSLSocketFactory delegate;

      public DelegatingSSLSocketFactory(SSLSocketFactory delegate) {
         this.delegate = delegate;
      }

      @Override
      public String[] getDefaultCipherSuites() {
         return delegate.getDefaultCipherSuites();
      }

      @Override
      public String[] getSupportedCipherSuites() {
         return delegate.getSupportedCipherSuites();
      }

      @Override
      public Socket createSocket(
          Socket socket, String host, int port, boolean autoClose) throws IOException {
         SSLSocket sslSocket = configureSocket((SSLSocket) delegate.createSocket(socket, host, port, autoClose));
         sslSocket.setEnabledProtocols(new String[]{TLS_v1_2});
         return sslSocket;
      }

      @Override
      public Socket createSocket(String host, int port) throws IOException {
         SSLSocket sslSocket = configureSocket((SSLSocket) delegate.createSocket(host, port));
         sslSocket.setEnabledProtocols(new String[]{TLS_v1_2});
         return sslSocket;
      }

      @Override
      public Socket createSocket(
          String host, int port, InetAddress localHost, int localPort) throws IOException {
         SSLSocket sslSocket = configureSocket((SSLSocket) delegate.createSocket(host, port, localHost, localPort));
         sslSocket.setEnabledProtocols(new String[]{TLS_v1_2});
         return sslSocket;
      }

      @Override
      public Socket createSocket(InetAddress host, int port) throws IOException {
         SSLSocket sslSocket = configureSocket((SSLSocket) delegate.createSocket(host, port));
         sslSocket.setEnabledProtocols(new String[]{TLS_v1_2});
         return sslSocket;
      }

      @Override
      public Socket createSocket(
          InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
         SSLSocket sslSocket = configureSocket((SSLSocket) delegate.createSocket(address, port, localAddress, localPort));
         sslSocket.setEnabledProtocols(new String[]{TLS_v1_2});
         return sslSocket;
      }

      protected SSLSocket configureSocket(SSLSocket socket) throws IOException {
         return socket;
      }
   }
}