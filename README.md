# Operating Systems Assignment

## Using semaphores to solve synchronization problems

Propose a synchronization solution to the following recipe cooking problem. Three chefs (threads) want to cook a recipe of meat, rice and vegetables. Each chef has an infinite supply of one of the ingredients and will need the other two ingredients from the supermarket. Assume chef1 has meat, chef2 has rice, and chef3 has vegetables. Each day, the supermarket (thread) puts two of the ingredients, chosen at random, on sale. Chefs strictly buy ingredients when they are on sale, which means only one chef will be able to cook on a given day. How can we use semaphores to solve this problem? All chef threads should be blocked waiting for the daily supermarket sale and only one chef per day will be able to cook. Assume that every recipe ingredient, namely meat, rice, and vegetables, has its own semaphore. Only one chef can acquire an ingredient. Every chef will need to wait for two ingredients signaled by the supermarket thread. 