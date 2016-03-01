package nl.jk5.pumpkin.server.scripting.network;

public final class Networks {

    public static NodeBuilder newNode(Environment host){
        return new NodeBuilder(host);
    }

    public static class NodeBuilder {

        private final Environment host;

        public NodeBuilder(Environment host) {
            this.host = host;
        }

        public ComponentBuilder withComponent(String name){
            return new ComponentBuilder(this.host, name);
        }

        public MutableNode create(){
            return new MutableNode(host);
        }
    }

    public static class ComponentBuilder {

        private final Environment host;
        private final String name;

        public ComponentBuilder(Environment host, String name) {
            this.host = host;
            this.name = name;
        }

        public SimpleComponent create(){
            return new SimpleComponent(host, name);
        }
    }
}
