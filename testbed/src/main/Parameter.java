package main; 
  
  
import java.text.DecimalFormat; 
import java.util.ArrayList; 
import java.util.HashMap; 
import java.util.List; 
  
import distributions.PseudoRandom; 
  
  
  
public class Parameter { 
      
    /*                        Market information                       */
    // flag for default or customised environment// 
    public static boolean ENV_EMPTY = true; 
    public static boolean ATK_EMPTY = true; 
    public static boolean DEF_EMPTY = true; 
    public static boolean EVA_EMPTY = true; 
    public static boolean FIRST_CONFIG_SE = true; 
    public static boolean FIRST_CONFIG_RE = true; 
    public static boolean ENV_IS_REAL = false; 
  
    //Marketplace Configuration// 
    public static int CREDITS_PER_TURN = 100; 
    public static String RATING_TYPE = "multinominal"; //"binary", "multinominal", "real" 
      
    //Agent Configuration// 
    public static int TOTAL_NO_OF_BUYERS =30; 
    public static int TOTAL_NO_OF_SELLERS =15; 
    public static int NO_OF_HONEST_BUYERS = 20; 
    public static int NO_OF_DISHONEST_BUYERS = 10; 
    public static int NO_OF_HONEST_SELLERS = 10; 
    public static int NO_OF_DISHONEST_SELLERS = 5; 
    public static int NO_OF_CRITERIA = 1; 
    public static double INITIAL_BALANCE = 100; 
    public static int TOTAL_RATINGS = 1000; 
    public static int MULTINOMINAL_RATING_LEVEL = 5; 
    //There are probability (p) transactions with target sellers and (1-p) with common sellers. 
    public static double m_honestBuyerOntargetSellerRatio = 0.5;  
    //The probability (p) attack to target sellers and (1-p) to common sellers. 
    public static double m_dishonestBuyerOntargetSellerRatio = 1 - m_honestBuyerOntargetSellerRatio; 
      
    //Product Configuration 
    public static int transaction_limit = 15; 
    public static int product_buy_limit = 50; 
    public static int product = 10000; 
    public static double max_price = 1000.00; 
    public static double min_price = 1.00; 
          
    //Scheduler Configuration 
    public static int NO_OF_DAYS = 10; 
    public static int NO_OF_RUNTIMES = 2; 
    
    //For real env
    public static String IMPORT_REAL_FILE = "";
    
    //Attack and Defense Model Name(s) 
    public static List atkNameList = null; 
    public static List defNameList = null; 
    public static List evaNameList = null; 
  
    //BRS Configuration 
    public static double BRS_quantile = 0.01; 
      
    //Travos Configuration 
    public static int NO_OF_BINS = 3; 
    public static double error_threshold = 0.2; 
    public static double minAccValue = 0.5; 
      
    //IClub Configuration 
    public static int iclub_epsilon = 3; 
      
    //Personalized Configuration 
    public static double p_Epsilon = 0.25; 
    public static double p_Gamma = 0.8; 
    public static double p_Forgetting = 0.5; 
    public static int p_TimeWindow = 10; 
      
    //ProbCog Configuration 
    public static double m_mu = 0.4;                //parameter in ProbCog, default is 0.6 
      
    //WMA Configuration 
    public static int neighbourLimit = 4; 
    public static int depthLimit = 6; 
      
    //MeTrust Configuration 
    public static String privilegedStrat = "Belief"; 
    public static String subStrat = "Primary"; 
    public static double lambda = 0; 
    public static double trustThreshold = 0.5; 
      
    //MSR Configuration 
    public static ArrayList<Double> MSR_imp_weight = new ArrayList<Double>(); 
    public static ArrayList<Double> MSR_pre_weight= new ArrayList<Double>(); 
      
          
              
    //number of time windows for personalized approach, estimator, rating type 
    public static final int m_timewindows = 10; 
    public static final double m_laplace = 1e-6; //reputation fix value 1/1e-6   
      
    /*************fixed values, unless big change**************/    
    //test the target method, target the first seller (honest) and the last seller (dishonest).  
    public static int TARGET_DISHONEST_SELLER = 0; 
    public static int TARGET_HONEST_SELLER = NO_OF_DISHONEST_SELLERS + NO_OF_HONEST_SELLERS - 1; 
      
    public static void changeTargetValues(){ 
        TARGET_DISHONEST_SELLER = 0; 
        TARGET_HONEST_SELLER = NO_OF_DISHONEST_SELLERS + NO_OF_HONEST_SELLERS - 1; 
    } 
      
    public static void updateValues(){ 
        TARGET_HONEST_SELLER = NO_OF_DISHONEST_SELLERS + NO_OF_HONEST_SELLERS - 1; 
        m_dishonestBuyerOntargetSellerRatio = 1 - m_honestBuyerOntargetSellerRatio; 
    } 
      
    //rating type, store in the instances    
    public static final int[] RATING_BINARY = {-1, 0, 1};  //it means {negative, null, positive} 
    public static final int[] RATING_MULTINOMINAL = {1 ,2, 3, 4, 5}; 
    public static final double[] RATING_REAL = {0.0, 1.0}; 
    //transfer real rating to binary rating 
    public static final double[] m_omega = {0.4, 0.6};  
    public static final String agent_dishonest = "dishonest"; 
    public static final String agent_honest = "honest";  
      
    //database translate into the instances 
    public static final String dayString = "day";  
    public static final String buyerIdString = "buyer_id"; 
    public static final String buyerHonestyString = "buyer_is_honest"; 
    public static final String sellerIdString= "seller_id"; 
    public static final String sellerHonestyString = "seller_is_honest"; 
    public static final String ratingString = "rating"; 
    public static final String salePriceString = "saleprice"; 
    public static final String productString = "product"; 
    public static final String sellerBalString = "sellerbalance"; 
    public static final String buyerBalString = "buyerbalance"; 
      
    //same as the database settings; 
    public static final int m_dayIdx = 0; 
    public static final int m_bidIdx = 1; 
    public static final int m_bbalIdx = 2; 
    public static final int m_bHonestIdx = 2; 
    public static final int m_pIdx = 3; 
    public static final int m_sidIdx = 3; 
    public static final int m_ppriceIdx = 4; 
    public static final int m_sHonestIdx = 4;    
    public static final int m_sidIdx2 = 5; 
    public static final int m_ratingIdx = 5; 
    public static final int m_sHonestIdx2 = 6; 
    public static final int m_sbalIdx = 7; 
  
    /*************fixed values, unless big change**************/
      
    public static final boolean includeWhitewashing(){ 
          
//      "alwaysUnfair", "camouflage", "whitewashing",  "sybil",  "sybil_camouflage", "sybil_whitewashing",  
        boolean whiteWashPresent = false; 
        for (int i = 0; i < atkNameList.size(); i++) 
        { 
            if(((String) atkNameList.get(i)).equalsIgnoreCase("whitewashing") || ((String) atkNameList.get(i)).equalsIgnoreCase("whitewashingselective") || ((String) atkNameList.get(i)).equalsIgnoreCase("sybil_whitewashing")){           
                whiteWashPresent = true; 
            } 
        } 
          
        return whiteWashPresent; 
    } 
      
    public static final boolean includeSybil(String attackName){ 
          
//      "alwaysUnfair", "camouflage", "whitewashing",  "sybil",  "sybil_camouflage", "sybil_whitewashing",  
        boolean include = false;         
        if(attackName.equalsIgnoreCase("sybil") || attackName.equalsIgnoreCase("sybilselective") || attackName.equalsIgnoreCase("sybil_whitewashing") || attackName.equalsIgnoreCase("sybil_camouflage") || attackName.equalsIgnoreCase("sybil_betray")){            
            include = true; 
        } 
          
        return include; 
    } 
      
    public static final boolean includeBRS(String defenseName){      
        //      "nodefense", "brs", "iclub", "travos", "personalized", "mit", "brs_travos", "brs_personalized", "iclub_travos", "iclub_personalized", 
        //      "travos_brs", "travos_iclub", "personalized_brs", "personalized_iclub", 
            boolean include = false; 
            if(defenseName.equalsIgnoreCase("brs")){ 
                include = true; 
            } 
              
            return include; 
        } 
      
    public static final boolean includeICLUB(String defenseName){        
    //      "nodefense", "brs", "iclub", "travos", "personalized", "mit", "brs_travos", "brs_personalized", "iclub_travos", "iclub_personalized", 
    //      "travos_brs", "travos_iclub", "personalized_brs", "personalized_iclub", 
        boolean include = false; 
        if(defenseName.equalsIgnoreCase("iclub") 
                || defenseName.equalsIgnoreCase("iclub_travos") || defenseName.equalsIgnoreCase("iclub_personalized") 
                || defenseName.equalsIgnoreCase("travos_iclub") || defenseName.equalsIgnoreCase("personalized_iclub")){ 
            include = true; 
        } 
          
        return include; 
    } 
  
    public static final boolean includeTRAVOS(String defenseName){       
//      "nodefense", "brs", "iclub", "travos", "personalized", "mit", "brs_travos", "brs_personalized", "iclub_travos", "iclub_personalized", 
//      "travos_brs", "travos_iclub", "personalized_brs", "personalized_iclub", 
        boolean include = false;         
        if(defenseName.equalsIgnoreCase("travos") 
                || defenseName.equalsIgnoreCase("brs_travos") || defenseName.equalsIgnoreCase("iclub_travos")  
                || defenseName.equalsIgnoreCase("travos_brs") || defenseName.equalsIgnoreCase("travos_iclub")){          
            include = true; 
        } 
          
        return include; 
    } 
      
    public static final boolean includePersonalized(String defenseName){         
    //      "nodefense", "brs", "iclub", "travos", "personalized", "mit", "brs_travos", "brs_personalized", "iclub_travos", "iclub_personalized", 
    //      "travos_brs", "travos_iclub", "personalized_brs", "personalized_iclub", 
        boolean include = false;         
        if(defenseName.equalsIgnoreCase("personalized") 
                || defenseName.equalsIgnoreCase("brs_personalized") || defenseName.equalsIgnoreCase("iclub_personalized")  
                || defenseName.equalsIgnoreCase("personalized_brs") || defenseName.equalsIgnoreCase("personalized_iclub")){          
            include = true; 
        } 
              
        return include; 
    } 
      
    public static final boolean includeWMA(String defenseName){ 
          
        boolean include = false;         
        if(defenseName.equalsIgnoreCase("wma")){             
            include = true; 
        } 
                  
        return include; 
    } 
      
    public static final boolean includeEA(String defenseName){       
  
        boolean include = false;         
        if(defenseName.equalsIgnoreCase("ea")|| defenseName.equalsIgnoreCase("ea0") || defenseName.equalsIgnoreCase("ea1") 
                || defenseName.equalsIgnoreCase("ea2")){             
            include = true; 
        } 
                  
        return include; 
    } 
  
    public static final double nullRating(){ 
          
        double nRating = 0.0; 
        if(Parameter.RATING_TYPE.equalsIgnoreCase("binary")){ 
            nRating = 0.0; 
        } else if(Parameter.RATING_TYPE.equalsIgnoreCase("multinominal")){ 
            int halfPos = Parameter.RATING_MULTINOMINAL.length / 2; 
            nRating = Parameter.RATING_MULTINOMINAL[halfPos]; 
        } else if(Parameter.RATING_TYPE.equalsIgnoreCase("real")){ 
            //nRating = PseudoRandom.randDouble(Parameter.m_omega[0], Parameter.m_omega[1]); 
            nRating = 0.5; 
        } else{ 
            System.out.println("not such type of rating"); 
        }        
          
        return nRating; 
    } 
      
    public static final int rating_binary2multinominal(double binaryVal){ 
          
        //[-1, 0, 1] means [1, 3, 5]     
        int halfPos = (Parameter.RATING_MULTINOMINAL.length)/2; 
        int mnVal = Parameter.RATING_MULTINOMINAL[halfPos]; 
          
        if(binaryVal == Parameter.RATING_BINARY[0]){ 
            mnVal = Parameter.RATING_MULTINOMINAL[0]; 
        } else if(binaryVal == Parameter.RATING_BINARY[1]){ 
            mnVal = Parameter.RATING_MULTINOMINAL[halfPos]; 
        } else if(binaryVal == Parameter.RATING_BINARY[2]){ 
            mnVal = Parameter.RATING_MULTINOMINAL[Parameter.RATING_MULTINOMINAL.length - 1]; 
        } 
          
        return mnVal; 
    } 
      
    public static final double rating_binary2real(double binaryVal){ 
          
        //[-1, 0, 1] means [0, [omega[0], omega[1],  1]      
        double realVal = PseudoRandom.randDouble(Parameter.m_omega[0], Parameter.m_omega[1]); 
          
        if(binaryVal == Parameter.RATING_BINARY[0]){ 
            realVal = Parameter.RATING_REAL[0]; 
        } else if(binaryVal == Parameter.RATING_BINARY[1]){ 
//          realVal = PseudoRandom.randDouble(Parameter.m_omega[0], Parameter.m_omega[1]); 
            realVal = 0.5; 
        } else if(binaryVal == Parameter.RATING_BINARY[2]){ 
            realVal = Parameter.RATING_REAL[1]; 
        } 
          
        return realVal; 
    } 
      
    public static final int rating_multinominal2binary(double mnVal){ 
          
        //{1 ,2, 3, 4, 5} means {neg,neg, null, pos, pos} = {-1, -1, 0, 1, 1} 
        int halfPos = (Parameter.RATING_MULTINOMINAL.length)/2; 
        int binaryVal = Parameter.RATING_BINARY[1]; 
        if(mnVal < halfPos){ 
            binaryVal = Parameter.RATING_BINARY[0]; 
        }else if(mnVal == halfPos){ 
            binaryVal = Parameter.RATING_BINARY[1]; 
        }else{ 
            binaryVal = Parameter.RATING_BINARY[2]; 
        } 
          
        return binaryVal; 
    } 
      
    public static final double rating_multinominal2real(double mnVal){ 
          
        //{1 ,2, 3, 4, 5} means  [0, 0.25, 0.5, 0.75, 1]     
        int mnrLen = Parameter.RATING_MULTINOMINAL.length; 
        double realVal = (mnVal - 1.0) * (Parameter.RATING_REAL[1] - Parameter.RATING_REAL[0]) / (mnrLen - 1.0); 
          
        return realVal; 
    } 
      
    public static final int rating_real2binary(double realVal){ 
          
        //{0.0, 1.0} means {neg, null, pos} = {-1, 0, 1}, 0: [omega1, omega2]        
        int binaryVal = Parameter.RATING_BINARY[1]; 
        if(realVal <= Parameter.m_omega[0]){ 
            binaryVal = Parameter.RATING_BINARY[0]; 
        }else if(realVal >= Parameter.m_omega[0] && realVal <= Parameter.m_omega[1]){ 
            binaryVal = Parameter.RATING_BINARY[1]; 
        }else{ 
            binaryVal = Parameter.RATING_BINARY[2]; 
        } 
          
        return binaryVal; 
    } 
      
    public static final int rating_real2multinominal(double realVal){ 
          
        //{0.0, 1.0} means {1 ,2, 3, 4, 5} = [0, 0.2),[0.2, 0.4), [0.4, 0.6), [0.6, 0.8), [0.8, 1.0]   
        double interval = (Parameter.RATING_REAL[1] - Parameter.RATING_REAL[0]) / (Parameter.RATING_MULTINOMINAL.length); 
        int mnVal = (int)(realVal / interval) + 1; 
        if(mnVal > Parameter.RATING_MULTINOMINAL.length){ 
            mnVal = Parameter.RATING_MULTINOMINAL.length; 
        } 
          
        return mnVal; 
    } 
  
    public static void setNO_OF_DISHONEST_BUYERS(int nO_OF_DISHONEST_BUYERS) { 
        NO_OF_DISHONEST_BUYERS = nO_OF_DISHONEST_BUYERS; 
    } 
  
    public static void setNO_OF_HONEST_BUYERS(int nO_OF_HONEST_BUYERS) { 
        NO_OF_HONEST_BUYERS = nO_OF_HONEST_BUYERS; 
    } 
  
    public static void setNO_OF_DISHONEST_SELLERS(int nO_OF_DISHONEST_SELLERS) { 
        NO_OF_DISHONEST_SELLERS = nO_OF_DISHONEST_SELLERS; 
    } 
  
    public static void setNO_OF_HONEST_SELLERS(int nO_OF_HONEST_SELLERS) { 
        NO_OF_HONEST_SELLERS = nO_OF_HONEST_SELLERS; 
    } 
  
    public static void setNO_OF_DAYS(int nO_OF_DAYS) { 
        NO_OF_DAYS = nO_OF_DAYS; 
    } 
  
    public static void setRATING_TYPE(String rATING_TYPE) { 
        RATING_TYPE = rATING_TYPE; 
    } 
  
    public static void setM_honestBuyerOntargetSellerRatio( 
            double m_honestBuyerOntargetSellerRatio) { 
        Parameter.m_honestBuyerOntargetSellerRatio = m_honestBuyerOntargetSellerRatio; 
    } 
  
    public static void setM_dishonestBuyerOnTargetSellerRatio( 
            double mDishonestbuyerontargetsellerratio) { 
        m_dishonestBuyerOntargetSellerRatio = mDishonestbuyerontargetsellerratio; 
    } 
  
    public static void setCREDITS_PER_TURN(int cREDITS_PER_TURN) { 
        CREDITS_PER_TURN = cREDITS_PER_TURN; 
    } 
  
    public static void setINITIAL_BALANCE(double iNITIAL_BALANCE) { 
        INITIAL_BALANCE = iNITIAL_BALANCE; 
    } 
  
    public static void setTransLimit(int transLimit) { 
        Parameter.transaction_limit = transLimit; 
    } 
  
    public static void setProductBuyLimit(int buy_limit) { 
        Parameter.product_buy_limit = buy_limit; 
    } 
  
    public static void setProduct(int product) { 
        Parameter.product = product; 
    } 
  
    public static void setMax_price(double max_price) { 
        Parameter.max_price = max_price; 
    } 
  
    public static void setMin_price(double min_price) { 
        Parameter.min_price = min_price; 
    } 
  
    public static void setTOTAL_NO_OF_BUYERS(int tOTAL_NO_OF_BUYERS) { 
        TOTAL_NO_OF_BUYERS = tOTAL_NO_OF_BUYERS; 
    } 
  
    public static void setTOTAL_NO_OF_SELLERS(int tOTAL_NO_OF_SELLERS) { 
        TOTAL_NO_OF_SELLERS = tOTAL_NO_OF_SELLERS; 
    } 
  
    public static void setBRS_quantile(double bRS_quantile) { 
        BRS_quantile = bRS_quantile; 
    } 
      
    public static void setMu_value(double M_mu) { 
        m_mu = M_mu; 
    } 
    public static void setNO_OF_BINS(int nO_OF_BINS) { 
        NO_OF_BINS = nO_OF_BINS; 
    } 
  
    public static void setError_threshold(double error_threshold) { 
        Parameter.error_threshold = error_threshold; 
    } 
  
    public static void setMinAccValue(double minAccValue) { 
        Parameter.minAccValue = minAccValue; 
    } 
  
    public static void setP_Epsilon(double p_Epsilon) { 
        Parameter.p_Epsilon = p_Epsilon; 
    } 
  
    public static void setP_Gamma(double p_Gamma) { 
        Parameter.p_Gamma = p_Gamma; 
    } 
  
    public static void setP_Forgetting(double p_Forgetting) { 
        Parameter.p_Forgetting = p_Forgetting; 
    } 
  
    public static void setP_TimeWindow(int p_TimeWindow) { 
        Parameter.p_TimeWindow = p_TimeWindow; 
    } 
  
    public static void setNO_OF_RUNTIMES(int nO_OF_RUNTIMES) { 
        NO_OF_RUNTIMES = nO_OF_RUNTIMES; 
    } 
  
    public static void setNeighbourLimit(int neighbourLimit) { 
        Parameter.neighbourLimit = neighbourLimit; 
    } 
  
    public static void setDepthLimit(int depthLimit) { 
        Parameter.depthLimit = depthLimit; 
    } 
  
    public static void setIclub_epsilon(int iclub_epsilon) { 
        Parameter.iclub_epsilon = iclub_epsilon; 
    } 
  
    public static void setMSR_imp_weight(ArrayList<Double> imp_weight){ 
        for (int i=0;i<Parameter.NO_OF_CRITERIA;i++){ 
            Parameter.MSR_imp_weight.add(imp_weight.get(i)); 
        } 
    } 
      
    public static void setMSR_pre_weight(ArrayList<Double> pre_weight){ 
        for (int j=0;j<Parameter.NO_OF_CRITERIA;j++){ 
            Parameter.MSR_pre_weight.add(pre_weight.get(j)); 
        } 
    } 
      
} 