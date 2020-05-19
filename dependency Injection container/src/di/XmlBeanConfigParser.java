package di;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class XmlBeanConfigParser implements BeanConfigParser{

    @Override
    public List<BeanDefinition> parse(InputStream inputStream) {
        List beanDefinitions = new ArrayList<>();

        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document doc = documentBuilder.parse(inputStream);

            doc.getDocumentElement().normalize();

            NodeList beanList = doc.getElementsByTagName("bean");

            for (int i = 0; i < beanList.getLength(); i++) {
                Node node = beanList.item(i);
                if (node.getNodeType() != Node.ELEMENT_NODE) continue;

                Element element = (Element) node;
                BeanDefinition beanDefinition = new BeanDefinition(
                        element.getAttribute("id"),
                        element.getAttribute("class")
                );
                if (element.getAttribute("scope").equals("singleton")) {
                    beanDefinition.setScope(BeanDefinition.Scope.SINGLETON);
                }
                if (element.getAttribute("lazy-init").equals("true")) {
                    beanDefinition.setLazyInit(true);
                }
                loadConstructorArgs(
                        element.getElementsByTagName("constructor-arg"),
                        beanDefinition
                );

                beanDefinitions.add(beanDefinition);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return beanDefinitions;
    }

    public void loadConstructorArgs(NodeList nodes, BeanDefinition beanDefinition) {
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) continue;
            Element element = (Element) node;

            BeanDefinition.ConstructorArg constructorArg = null;
            if (element.getAttribute("type").equals("String")) {
                constructorArg = new BeanDefinition.ConstructorArg(String.class, element.getAttribute("value"));
            }
            else if(element.getAttribute("type").equals("int"))
            {
                constructorArg = new BeanDefinition.ConstructorArg(Integer.class, Integer.valueOf(element.getAttribute("value")) );
            }

            if (!element.getAttribute("ref").isEmpty()) {
                constructorArg = new BeanDefinition.ConstructorArg(true, element.getAttribute("ref"));            }

            beanDefinition.addConstructorArg(constructorArg);
        }
    }
}
