README


To use: 

>Include all the files in the given testbed folder
>Include the necessary libraries from the library folder
>Run MainGUI.java from the GUI package

-----------------------------------------------------------------------------------------------

 To import attack models,
> Import file must be of .CLASS format
	>Methods to be included in .CLASS file
		>double giveUnfairRating(Instance inst)
		>Instance chooseSeller(int day, Buyer b, Environment env);
>Alternatively you can include the attack model under package attacks extending Attack.java 
	>include the attack name in atkModels.txt under \SavedConfiguration\

-----------------------------------------------------------------------------------------------
 To import defense models,
>Import file must be of .CLASS format
	>Methods to be included in .CLASS file
		>double calculateTrust(Seller s, Buyer b, int criteriaid);
		> Instance chooseSeller(int day, Buyer b, Environment env);
		>double predictRealSellerReputation(Buyer b, Environment env, Seller s, int criteriaid);
>Alternatively you can include the defense model under package defenses extending Defense.java 
	>include the attack name in defModels.txt under \SavedConfiguration\

-----------------------------------------------------------------------------------------------
To create real environments
>Import real environment configuration under \data\realdata\savedConfiguration\XXXConfig.dat


