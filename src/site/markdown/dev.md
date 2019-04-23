### Compiling

cd into `encrypt` top level directory and then run:

```bash
mvn -am clean install
```

### Generating Documentation

To generate this html site documentation, run the following:

```bash
mvn site:site
```

This will create an index.html file under `target/site` that you can open in your browser.

### Building an Installation Package

You can build `zip` and `tar.gz` packages for this library by running the following

```bash
mvn assembly:single
```