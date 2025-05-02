module ru.npsystems {
    requires java.base;
    requires transitive java.xml;
    requires transitive java.xml.crypto;

    exports ru.npsystems.hash;
    exports ru.npsystems.transform;
}